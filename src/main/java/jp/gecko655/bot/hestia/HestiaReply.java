package jp.gecko655.bot.hestia;


import java.text.DateFormat;
import java.util.List;
import java.util.TimeZone;
import java.util.logging.Level;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

import jp.gecko655.bot.AbstractCron;
import jp.gecko655.bot.DBConnection;
import twitter4j.Paging;
import twitter4j.Relationship;
import twitter4j.Status;
import twitter4j.StatusUpdate;
import twitter4j.TwitterException;

public class HestiaReply extends AbstractCron {
    
    static final DateFormat format = DateFormat.getDateTimeInstance(DateFormat.FULL, DateFormat.FULL);
    private static final Pattern whoPattern = Pattern.compile("( 誰$| だれ$|誰[^だで]|だれ[^だで]|誰だ[^と]?|だれだ[^と]?| 違う| ちがう)");

    public HestiaReply() {
        format.setTimeZone(TimeZone.getDefault());
    }

    @Override
    protected void twitterCron() {
        try {
            Status lastStatus = DBConnection.getLastStatus();
            List<Status> replies = twitter.getMentionsTimeline((new Paging()).count(20));
            if(replies.isEmpty()){
                logger.log(Level.INFO, "Not yet replied. Stop.");
                return;
            }
            DBConnection.setLastStatus(replies.get(0));
            if(lastStatus == null){
                logger.log(Level.INFO,"memcache saved. Stop. "+replies.get(0).getUser().getName()+"'s tweet at "+format.format(replies.get(0).getCreatedAt()));
                return;
            }
            List<Status> validReplies = replies.stream().filter(reply -> isValid(reply, lastStatus)).collect(Collectors.toList());
            if(validReplies.isEmpty()){
                logger.log(Level.FINE, "No valid replies. Stop.");
                return;
            }
            
            for(Status reply : validReplies){
                Relationship relation = twitter.friendsFollowers().showFriendship(twitter.getId(), reply.getUser().getId());
                if(!relation.isSourceFollowingTarget()){
                    twitter.createFriendship(reply.getUser().getId());
                }

                if(whoPattern.matcher(reply.getText()).find()){
                    // put latest image URL to black-list
                    who(reply);    
                }else{
                    //auto reply (when hestia-sama follows the replier)
                    StatusUpdate update= new StatusUpdate("@"+reply.getUser().getScreenName()+" ");
                    update.setInReplyToStatusId(reply.getId());
                    //if(((int) (Math.random()*10))==1){//10%
                    updateStatusWithMedia(update, "ヘスティア　ダンジョンに出会いを求めるのは間違っているだろうか",100);
                    //}else{
                        //updateStatusWithMedia(update, "藤宮香織 かわいい 一週間フレンズ。",100);
                    //}
                }
            }
        } catch (TwitterException e) {
            logger.log(Level.WARNING,e.toString());
            e.printStackTrace();
        }
    }

    private boolean isValid(Status reply, Status lastStatus) {
        if(lastStatus==null) return false;
        return reply.getCreatedAt().after(lastStatus.getCreatedAt());
    }

    private void who(Status reply) {
        //Store the url to the black list.
        DBConnection.storeImageUrlToBlackList(reply.getInReplyToStatusId(),reply.getUser().getScreenName());

        try{
            //Delete the reported tweet.
            twitter.destroyStatus(reply.getInReplyToStatusId());
            
            //Apologize to the report user.
            StatusUpdate update= new StatusUpdate("@"+reply.getUser().getScreenName()+" 間違えちゃった。ごめんね！");
            update.setInReplyToStatusId(reply.getId());
            twitter.updateStatus(update);
        }catch(TwitterException e){
            e.printStackTrace();
        }
    }
}
