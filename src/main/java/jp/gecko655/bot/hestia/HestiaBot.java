package jp.gecko655.bot.hestia;

import jp.gecko655.bot.AbstractCron;
import twitter4j.StatusUpdate;

public class HestiaBot extends AbstractCron{

    @Override
    protected void twitterCron() {
        //Twitterに書き出し
        StatusUpdate status = new StatusUpdate(" "); //$NON-NLS-1$
        //if(((int) (Math.random()*10))==1){//10%
        updateStatusWithMedia(status, "ヘスティア ダンジョンに出会いを求めるのは間違っているだろうか",100);
        //}else{
        //    updateStatusWithMedia(status, "藤宮さん 一週間フレンズ。", 100);
        //}
        
    }

}
