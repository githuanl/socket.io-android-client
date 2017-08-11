package com.centersoft.util;

import android.content.Context;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Looper;
import android.widget.Toast;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;

public class Tools {

    /**
     * 判断网络连接状态
     *
     * @param context
     * @return
     */
    public static boolean isConnected(Context context) {
        ConnectivityManager conn = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo info = conn.getActiveNetworkInfo();
        return (info != null && info.isConnected());
    }

    /**
     * @param toast
     * @param context
     * @throws
     * @Title 线程中显示toast
     * @Description TODO
     * @author LH
     * @Createdate 2015年4月11日 下午6:04:02
     */
    public static void showToast(final String toast, final Context context) {
        new Thread(new Runnable() {

            @Override
            public void run() {
                Looper.prepare();
                Toast.makeText(context, toast, Toast.LENGTH_SHORT).show();
                Looper.loop();
            }
        }).start();
    }


    public static String transTime(long infoTime){

        Date data=new Date();
        long l = 24*60*60*1000;

        String transtime = null;
        SimpleDateFormat todayform = new SimpleDateFormat("HH:mm");
        SimpleDateFormat dayform = new SimpleDateFormat("MM-dd HH:mm");

        Calendar cc = Calendar.getInstance();
        Long currentTimertrans=cc.getTimeInMillis();
        int currentWeek=cc.get(Calendar.DAY_OF_WEEK);

        //当日0晨时间
        /*Long currentTimer=cc.getTimeInMillis();*/
        Long currentTimer=data.getTime() - (data.getTime()%l)-8*60*60*1000;


        int days=(int)((currentTimer-infoTime)/(24*60*60*1000));//0是一天 1是两天 2是三天


        if (infoTime>=currentTimer){
            //今天
            transtime=todayform.format(new Date(infoTime));
        }
        else{
            //不是今天了，在今天之前的
            if ((currentTimer-infoTime)>=24*60*60*1000){
                //昨天之前的 之间间隔了一天多 不是昨天  开始进行星期的判断
                if (currentWeek==1){
                    //星期天：日期是周六之前的
                    if (days>5){
                        //显示月日时间
                        transtime=dayform.format(new Date(infoTime));

                    }else{
                        //显示星期
                        if (days==5){
                            //星期一
                            transtime="星期一 "+todayform.format(new Date(infoTime));
                        }
                        else if (days==4){
                            //星期二
                            transtime="星期二 "+todayform.format(new Date(infoTime));
                        }
                        else if (days==3){
                            //星期三
                            transtime="星期三 "+todayform.format(new Date(infoTime));
                        }
                        else if (days==2){
                            //星期四
                            transtime="星期四 "+todayform.format(new Date(infoTime));
                        }
                        else if (days==1){
                            //星期五
                            transtime="星期五 "+todayform.format(new Date(infoTime));
                        }
                        else if (days==0){
                            //星期六
                            transtime="星期六 "+todayform.format(new Date(infoTime));
                        }
                    }



                }else if (currentWeek==2){
                    //星期一:直接显示日期
                    transtime=dayform.format(new Date(infoTime));

                }else if (currentWeek==3){
                    //星期二：直接显示日期
                    transtime=dayform.format(new Date(infoTime));

                }else if (currentWeek==4){
                    //星期三
                    if (days>1){
                        transtime=dayform.format(new Date(infoTime));
                    }
                    else if (days==1){
                        transtime="星期一 "+todayform.format(new Date(infoTime));
                    }
                    else if (days==0){
                        //星期六
                        transtime="星期二 "+todayform.format(new Date(infoTime));
                    }
                }else if (currentWeek==5){
                    //星期四
                    if (days>2){
                        transtime=dayform.format(new Date(infoTime));
                    }
                    else if (days==2){
                        transtime="星期一 "+todayform.format(new Date(infoTime));
                    }
                    else if (days==1){
                        //星期六
                        transtime="星期二 "+todayform.format(new Date(infoTime));
                    }
                    else if (days==0){
                        //星期六
                        transtime="星期三 "+todayform.format(new Date(infoTime));
                    }
                }else if (currentWeek==6){
                    //星期五

                    if (days>3){
                        transtime=dayform.format(new Date(infoTime));
                    }
                    else if (days==3){
                        transtime="星期一 "+todayform.format(new Date(infoTime));
                    }
                    else if (days==2){
                        //星期六
                        transtime="星期二 "+todayform.format(new Date(infoTime));
                    }
                    else if (days==1){
                        //星期六
                        transtime="星期三 "+todayform.format(new Date(infoTime));
                    }
                    else if (days==0){
                        //星期六
                        transtime="星期四 "+todayform.format(new Date(infoTime));
                    }

                }else if (currentWeek==7){
                    //星期六
                    if (days>4){
                        transtime=dayform.format(new Date(infoTime));
                    }
                    else if (days==4){
                        transtime="星期一 "+todayform.format(new Date(infoTime));
                    }
                    else if (days==3){
                        //星期六
                        transtime="星期二 "+todayform.format(new Date(infoTime));
                    }
                    else if (days==2){
                        //星期六
                        transtime="星期三 "+todayform.format(new Date(infoTime));
                    }
                    else if (days==1){
                        //星期六
                        transtime="星期四 "+todayform.format(new Date(infoTime));
                    }
                    else if (days==1){
                        //星期六
                        transtime="星期五 "+todayform.format(new Date(infoTime));
                    }
                }

            }else {
                //是昨天
                transtime="昨天 "+todayform.format(new Date(infoTime));
            }

        }

        return transtime;
    }

}
