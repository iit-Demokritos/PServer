 /* 
 * Copyright 2011 NCSR "Demokritos"
 * 
 * Licensed under the Apache License, Version 2.0 (the "License");   
 * you may not use this file except in compliance with the License.   
 * You may obtain a copy of the License at
 * 
 *      http://www.apache.org/licenses/LICENSE-2.0
 *    
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 * 
 */
package pservertester;

import java.net.InetAddress;
import java.util.LinkedList;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 * @author alexandros
 */
public class Main {

    /**
     * @param args the command line arguments
     */
    public static void main(String[] args) {
        try {
            LinkedList<String> commands = new LinkedList();
            LinkedList<String> failedCommands = new LinkedList();
            LinkedList<String> correctCommands = new LinkedList();
            /*
             * Pers commands
             */
            //commands.add( "/pers?clnt=test|test&&com=caldcy&usr=1&grp=1);
            //commands.add( "/pers?clnt=test|test&com=getavg&ftr=speed.*" );
            //commands.add( "/pers?clnt=test|test&com=getdrt" );

            //commands.add("/pers?clnt=test|test&com=addattr&testAttr." + index +"=test");
            //commands.add( "/pers?clnt=test|test&com=remattr&attr=testAttr" + index +"");
            //commands.add( "/pers?clnt=test|test&com=addftr&testFtr" + index +"=0" )
            //commands.add( "/pers?clnt=test|test&com=remftr&ftr=testFtr" + index +"");;
            //commands.add( "/pers?clnt=test|test&com=getattrdef&attr=testFtr1" );
            //commands.add( "/pers?clnt=test|test&com=getftrdef&ftr=1" );
            //commands.add( "/pers?clnt=test|test&com=getusrattr&usr=1&attr=age*&attr=sex" );
            //commands.add( "/pers?clnt=test1|test1&com=getusrs&whr=*");
            //commands.add( "/pers?clnt=test|test&com=getusrs&whr=1");
            //commands.add( "/pers?clnt=test|test&com=setattr&usr=1&&age=20");
            //commands.add( "/pers?clnt=test|test&com=setattrdef&age=20&testAttr.*=testTest");
            //commands.add( "/pers?clnt=test|test&com=setftrdef&1=0&10=0");
            //commands.add( "/pers?clnt=test|test&com=sqlattrusr&whr=*");
            //commands.add( "/pers?clnt=test|test&com=sqlftrusr&whr=*");
            //commands.add( "/pers?clnt=test|test&com=setusr&usr=testUser&attr_age=10&attr_sex=male&ftr_1=1");
            //commands.add( "/pers?clnt=test|test&com=getusrftr&usr=1&ftr=1&ftr=10" );
            //commands.add( "/pers?clnt=test|test&com=addddt&usr=1&1=" + new Date().getTime());
            //commands.add( "/pers?clnt=test|test&com=addddt&usr=1&sid=0&1=" + new Date().getTime());
            //commands.add( "/pers?clnt=test|test&com=addddt&usr=testUser&sid=1&10=" + new Date().getTime());
            //commands.add( "/pers?clnt=test|test&com=addndt&usr=1&tms=" + new Date().getTime() + "&1=2.55" );
            //commands.add( "/pers?clnt=test|test&com=incval&usr=testUser&1asd=1&10=-0.15" );
            //commands.add( "/pers?clnt=test|test&com=remddt&whr=dd_user:'1'" );
            //commands.add( "/pers?clnt=test|test&com=remndt&whr=nd_user:'1'");
            //commands.add( "/pers?clnt=test|test&com=incval&usr=1&1=1&10=-0.15" );
            //commands.add( "/pers?clnt=test|test&com=remusr&usr=10" );
            //commands.add( "/pers?clnt=test|test&com=setdcy&1=0.5&10=1");
            //commands.add( "/pers?clnt=test|test&com=sqlddt&whr=*");
            //commands.add( "/pers?clnt=test|test&com=sqlndt&whr=*");
            //commands.add( "/pers?clnt=test|test&com=remdcy");


            /*
             * ster commands
             */
            //commands.add( "/ster?clnt=test|test&com=addstr&str=programmers&occupation=programmer");
            //commands.add( "/ster?clnt=test|test&com=remstr&str=programmer&lke=exper");
            //commands.add( "/ster?clnt=test|test&com=addusr&usr=1&programmers=0.78");
            //commands.add( "/ster?clnt=test|test&com=getstr&str=programmers&ftr=*");
            //commands.add( "/ster?clnt=test|test&com=getusr&usr=1&str=*");
            //commands.add( "/ster?clnt=test|test&com=incdeg&usr=1&programmers=-0.1");
            //commands.add( "/ster?clnt=test|test&com=lststr&str=*");
            //commands.add( "/ster?clnt=test|test&com=setdeg&usr=1&programmers=0.85" );
            //commands.add( "/ster?clnt=test|test&com=sqlstr&whr=*" );
            //commands.add( "/ster?clnt=test|test&com=sqlusr&whr=*" );
            //commands.add( "/ster?clnt=test|test&com=remusr&1=*" );
            //commands.add( "/ster?clnt=test|test&com=setstr&str=programmers&1=1" );
            //commands.add( "/ster?clnt=test|test&com=incval&str=programmers&1=-0.1&10=1");

            /*
             * commu commands
             */
            //commands.add( "/commu?clnt=test1|test1&com=calcudist&smetric=cos" );

            //System.out.println( "1" );
            //commands.add( "/commu?clnt=test1|test1&com=mkcom&algorithm=alexone&th=>0.7" );
            //commands.add( "/commu?clnt=test1|test1&com=mkcom&algorithm=bk&th=>0.5" );
            //commands.add( "/commu?clnt=test1|test1&com=mkcom&algorithm=bk&th=>0.2" );
            //commands.add( "/commu?clnt=test1|test1&com=mkcom&algorithm=bk&th=>0.1" );
            //System.out.println( "2" );
            //commands.add( "/commu?clnt=test1|test1&com=mkcom&algorithm=alexone&th=>0.2" );
            //System.out.println( "3" );
            //commands.add( "/commu?clnt=test1|test1&com=mkcom&algorithm=alexone&th=>0.3" );
            //System.out.println( "4" );
            //commands.add( "/commu?clnt=test1|test1&com=mkcom&algorithm=alexone&th=>0.4" );
            //System.out.println( "5" );
            //commands.add( "/commu?clnt=test1|test1&com=mkcom&algorithm=alexone&th=>0.5" );
            //System.out.println( "6" );
            //commands.add( "/commu?clnt=test1|test1&com=mkcom&algorithm=alexone&th=>0.6" );
            //System.out.println( "7" );
            //commands.add( "/commu?clnt=test1|test1&com=mkcom&algorithm=alexone&th=>0.7" );
            //System.out.println( "8" );
            //commands.add( "/commu?clnt=test1|test1&com=mkcom&algorithm=alexone&th=>0.8" );
            //System.out.println( "9" );
            //commands.add( "/commu?clnt=test1|test1&com=mkcom&algorithm=alexone&th=>0.9" );
            //System.out.println( "10" );
            //commands.add( "/commu?clnt=test1|test1&com=mkcom&algorithm=alexone&th=>1.0" );
            //commands.add( "/commu?clnt=test1|test1&com=rmftrgrp&grp=grp2" );
            //commands.add( "/commu?clnt=test1|test1&com=addftrgrp&ftrs=ftr1|ftr2|ftr3|ftr4&usrs=usr1|usr2|usr3&name=custom1" );

            //commands.add( "/commu?clnt=test|test&com=calcudist&smetric=cos" );
            //commands.add( "/commu?clnt=test|test&com=mkcom&algorithm=bk&th=>0.8" );
            //commands.add( "/commu?clnt=test|test&com=calcftrdist&smetric=cos" );
            //commands.add( "/commu?clnt=test|test&com=mkftrgrp&algorithm=bk&th=>0.5" );

            //commands.add( "/commu?clnt=test|test1&com=getftrgrps&usr=10" );
            //commands.add("/commu?clnt=Mydmoz|1234&com=getftrgrps&usr=fed");

            /*
             * csv commands
             */
            //commands.add( "/csv?clnt=test|test&com=loadftr&path=/home/alexm/workspacePServer/PServer/TestSets/huge/movies.dat&cs=::&ftrcol=0&defvalue=0.0" );
            //commands.add( "/csv?clnt=test|test&com=loadusr&path=/home/alexm/workspacePServer/PServer/TestSets/huge/ratings.dat&cs=::&usrcol=0" );
            //commands.add( "/csv?clnt=test|test&com=loadusr&path=/home/alexm/workspacePServer/PServer/TestSets/huge/ratings.dat&cs=::&usrcol=0" );
            //commands.add( "/csv?clnt=test|test&com=loadlog&path=/home/alexm/workspacePServer/PServer/TestSets/huge/ratings.dat&cs=::&usrcol=0&ftrcol=1&numcol=2&timecol=3&sesgen=1000000000" );
            //commands.add( "/csv?clnt=test1|test1&com=loadftr&path=/home/alexm/workspacePServer/PServer/TestSets/medium/movies.dat&cs=::&ftrcol=0&defvalue=0.0" );
            //commands.add( "/csv?clnt=test1|test1&com=loadusr&path=/home/alexm/workspacePServer/PServer/TestSets/huge/ratings.dat&cs=::&usrcol=0" );
            //commands.add( "/csv?clnt=test1|test1&com=loadlog&path=/home/alexm/workspacePServer/PServer/TestSets/medium/ratings.dat&cs=::&usrcol=0&ftrcol=1&numcol=2&timecol=3&sesgen=1000000000" );
            //commands.add( "/csv?clnt=test1|test1&com=loadftrgrp&path=/home/alexm/NetBeansProjects/pserver/PServer/PServer/test.csv&fs=\\|&cs=\\,&nmcol=0&ftrcol=1&usrcol=2" );
            //commands.add( "/csv?clnt=test1|test1&com=loadftrgrp&path=/home/alexm/NetBeansProjects/pserver/PServer/PServer/test.csv&fs=\\|&cs=\\,&nmcol=0&ftrcol=1" );


            /*
             * Movielens
             */
            //commands.add( "/movies?clnt=test1|test1&com=loadftr&path=/home/alexm/workspacePServer/PServer/TestSets/small/ml-data/u.item" );
            //commands.add( "/movies?clnt=test1|test1&com=loadusr&path=/home/alexm/workspacePServer/PServer/TestSets/small/ml-data/u.user" );
            //commands.add( "/movies?clnt=test1|test1&com=loadftr&path=/home/alexm/PServer/TestSets/small/ml-data/u.item" );
            //commands.add( "/movies?clnt=test1|test1&com=loadusr&path=/home/alexm/PServer/TestSets/small/ml-data/u.user" );
            //commands.add( "/movies?clnt=test1|test1&com=loadusr&path=/home/alexm/workspacePServer/PServer/TestSets/small/ml-data/u.user" );
            //commands.add( "/movies?clnt=test1|test1&com=copyDb" );
            //commands.add( "/movies?clnt=test1|test1&com=copyDb" );
            //commands.add( "/csv?clnt=test1|test1&com=loadusr&path=/home/alexm/workspacePServer/PServer/TestSets/small/ml-data/u.user&cs=\\|&usrcol=0" );
            //commands.add( "/csv?clnt=test1|test1&com=loadlog&path=/home/alexm/workspacePServer/PServer/TestSets/small/ml-data/u.data&cs=\t&usrcol=0&ftrcol=1&numcol=2&timecol=3&sesgen=1" );
            //commands.add( "/csv?clnt=test1|test1&com=loadlog&path=/home/alexm/PServer/TestSets/small/ml-data/u.data&cs=\t&usrcol=0&ftrcol=1&numcol=2&timecol=3&sesgen=1" );            
            //commands.add( "/csv?clnt=test1|test1&com=loadlog&path=/home/alexm/workspacePServer/test/repository/TestSets/small/ml-data/u.data75&cs=\\t&usrcol=0&ftrcol=1&numcol=2&timecol=3&sesgen=" + time + "&ren=movie." );


            //real test
            //commands.add( "/movies?clnt=test|test&com=loadusr&path=/home/alexm/workspacePServer/PServer/TestSets/medium/users.dat" );
            int time = 24 * 3600 * 7;
            //commands.add( "/csv?clnt=test|test&com=loadlog&path=/home/alexm/workspacePServer/PServer/TestSets/medium/test/u.data75&cs=::&usrcol=0&ftrcol=1&numcol=2&timecol=3&sesgen=" + time + "&ren=movie." );
            //commands.add( "/koychev?clnt=test|test&com=update&k=0.0&s0=0&s=" + Integer.MAX_VALUE );
            //commands.add( "/koychev?clnt=test|test&com=update&k=0.0&s0=0&s=" + Integer.MAX_VALUE );

            //calling simple spreading time window algorithm
            //commands.add( "/spr?clnt=test1|test1&com=update&t0=0&t=" + Integer.MAX_VALUE );
            //calling koychev algorithm
            //commands.add( "/koychev?clnt=test1|test1&com=update&k=0.6&s0=0&s=" + Integer.MAX_VALUE );
            //commands.add( "/koychev?clnt=test1|test1&com=update&k=0.2&s0=0&s=" + Integer.MAX_VALUE );
            //commands.add( "/koychev?clnt=test1|test1&com=update&k=0.5&s0=0&s=" + Integer.MAX_VALUE );
            //commands.add( "/koychev?clnt=test1|test1&com=update&k=1&s0=0&s=" + Integer.MAX_VALUE );
            //commands.add( "/koychev?clnt=test1|test1&com=update&k=5&s0=0&s=" + Integer.MAX_VALUE );
            //commands.add( "/koychev?clnt=test1|test1&com=update&k=20&s0=0&s=" + Integer.MAX_VALUE );
            //commands.add( "/nootropia?clnt=test1|test1&com=genweights" );
            //commands.add( "/nootropia?clnt=test1|test1&com=genweights&base=movie.*" );

            //commands.add( "/nootropia?clnt=test1|test1&com=genweights" );
            //commands.add( "/nootropia?clnt=test1|test1&com=genpersweights" );
            //commands.add( "/madgik?clnt=madgik|madgik&com=loadlog&path=/home/alexm/outlogs3.txt" );
            //commands.add( "/madgik?clnt=test1|test1&com=updateprofiles&t0=0&t=" + Integer.MAX_VALUE );
            //commands.add( "/madgik?clnt=test1|test1&com=recquery&user=alexm@di.uoa.gr&query=movie" );

            //commands.add( "/madgik?clnt=madgik|madgik&com=updateprofiles&t0=0&t=" + Integer.MAX_VALUE );
            //commands.add( "/madgik?clnt=madgik|madgik&com=recquery&user=&query=work" );
            //commands.add( "/nootropia?clnt=test1|test1&com=genweights" );
            //commands.add( "/nootropia?clnt=test1|test1&com=genpersweights" );
            
            //commands.add( "/ster?clnt=test|test&com=addstr&str=educated2&rule=gender:45" );
            //commands.add("/ster?clnt=test|test&com=addusr&usr=1001&educated2=1");
            //commands.add("/ster?clnt=test|test&com=incdeg&usr=1001&educated2=-0.12");
            //commands.add("/ster?clnt=test|test&com=remusr&1001=educated2");
            //commands.add("/ster?clnt=test|test&com=setdeg&usr=0.34");
            //commands.add("/pers?clnt=test|test&com=addftr&lang.en=0&lang.fr=0&lang.gr=1");
            //commands.add("/pers?clnt=test|test&com=remftr&ftr=lang.en&ftr=lang.fr&ftr=lang.gr");
            //commands.add("/pers?clnt=test|test&com=setusr&usr=918&movie.999=4");
            commands.add("/pers?clnt=test|test&com=incval&usr=918&movie.999=4");
            for (String command : commands) {
                long t = System.currentTimeMillis();
                PSClientRequest request = new PSClientRequest(InetAddress.getLocalHost(), 1111, command, true, 10000);
                for (int i = 0; i < request.getRows(); i++) {
                    System.out.println(request.getValue(i, 0));
                }
                //System.out.println( "content = " + request.getResponse() );
                System.out.println(" time passed " + (System.currentTimeMillis() - t) / (1000 * 60.0));
                if (request.isError() == true) {
                    failedCommands.add(command + " \n ======= \n " + request.getErrorMessage() + " \n ======= \n " + request.getResponse());
                } else {
                    correctCommands.add(command);
                }
            }

            System.out.println("Successfull commands");
            for (String command : correctCommands) {
                System.out.println(command);
            }

            System.out.println("Failed commands");
            for (String command : failedCommands) {
                System.out.println(command);
            }

            StringBuffer a = new StringBuffer();
            a.append("1").append("2").append(3);
            System.out.println(a.toString());
        } catch (Exception ex) {
            Logger.getLogger(Main.class.getName()).log(Level.SEVERE, null, ex);
        }
    }
}
