/*******************************************************************************
 * Copyright 2013-2014 alladin-IT GmbH
 * 
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *   http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 ******************************************************************************/
package at.alladin.rmbt.statisticServer;

import java.sql.Date;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.AbstractMap;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.restlet.resource.Get;

//Statistics for internal purpose
//breaks the mvvm-pattern

public class UsageResource extends ServerResource
{
    private final String webRoot = "en";
    @Get("html")
    public String request(final String entity)
    {
        final StringBuilder result = new StringBuilder();
        
        try
        {
            PreparedStatement ps;
            ResultSet rs;
            String sql;
            List<Map.Entry<Long,Long>> statTests = new ArrayList<>();
            List<Map.Entry<Long,Long>> statClients = new ArrayList<>();
            List<Map.Entry<Long,Long>> statIPs = new ArrayList<>();
            
            //select statistics for last 30 days
            final String select = "count(uid) count_tests, count(DISTINCT client_id) count_clients, count(DISTINCT client_public_ip) count_ips";
            final String where = "status='FINISHED' AND deleted=false";
            
            sql = String.format("select date_trunc('day', time) _day, %s from test where %s AND time > current_date - interval '30 days' group by _day ORDER by _day DESC", select, where);
            ps = conn.prepareStatement(sql);
            ps.execute();
         
            result.append(getHeader());
            result.append("<thead><tr><th>Date</th><th class=\"r\">#tests</th><th class=\"r\">#clients</th><th class=\"r\">#ips</th></tr></thead>\n");
            result.append("<tbody>\n");
            
            rs = ps.getResultSet();
            while (rs.next())
            {
                final Date day = rs.getDate("_day");
                final long countTests = rs.getLong("count_tests");
                final long countClients = rs.getLong("count_clients");
                final long countIPs = rs.getLong("count_ips");
                final String searchlink = webRoot + "/Opentests?time%5B%5D=>" + day.getTime() + "&amp;time%5B%5D=<" + (day.getTime()+(1000*60*60*24));
                statTests.add(new AbstractMap.SimpleEntry<>(day.getTime(), countTests));
                statClients.add(new AbstractMap.SimpleEntry<>(day.getTime(), countClients));
                statIPs.add(new AbstractMap.SimpleEntry<>(day.getTime(), countIPs));
                //result.append(String.format("%s: % 8d  % 8d  %8d\n", day, countTests, countClients, countIPs));
                result.append(String.format("<tr><td><a href=\"%s\">%s</a></td> <td class=\"r\">%8d</td>  <td class=\"r\">%8d</td>  <td class=\"r\">%8d</td></tr>\n", searchlink, day, countTests, countClients, countIPs));
            }
            ps.close();
            
            sql = String.format("select %s from test where %s", select, where);
            ps = conn.prepareStatement(sql);
            ps.execute();
            
            result.append("\n");
            
            rs = ps.getResultSet();
            if (rs.next())
            {
                final long countTests = rs.getLong("count_tests");
                final long countClients = rs.getLong("count_clients");
                final long countIPs = rs.getLong("count_ips");
                result.append(String.format("<tr class=\"info\"><td>Total</td><td class=\"r\">%8d</td><td class=\"r\">%8d</td><td class=\"r\">%8d</td></tr>\n", countTests, countClients, countIPs));
            }
            
            //remove the last day since the day is not yet over (=first in the array)
            if (statTests.size() > 0)
            {
                statTests.remove(0);
                statClients.remove(0);
                statIPs.remove(0);
            }
            
            result.append("</tbody></table><div id='flot' style='height:450px;width:100%'></div></div>").append(makeStat(statTests,statClients,statIPs)).append(getFooter());
            ps.close();
        }
        catch (SQLException e)
        {
            e.printStackTrace();
        }
        
        
        
        return result.toString();
    }
    
    /**
     * Generates the javascript-code necessary for generating a flot diagram
     * @param tests list of test count with corresponding timestamp
     * @param clients 
     * @param ips
     * @return the generated string from < script> to </ script>
     */
    private static String makeStat(List<Map.Entry<Long,Long>> tests, List<Map.Entry<Long,Long>> clients, List<Map.Entry<Long,Long>> ips) {
        //generate arrays for javascript in form [[11,1],[12,2],...]
        String t = "[";
        for (Map.Entry<Long, Long> entry : tests) {
            t += "[" + entry.getKey() + "," + entry.getValue() + "],";
        }
        t = t.substring(0,t.length()-1); //trim last comma
        t += "]";
        
        String c = "[";
        for (Map.Entry<Long, Long> entry : clients) {
            c += "[" + entry.getKey() + "," + entry.getValue() + "],";
        }
        c = c.substring(0,c.length()-1);
        c += "]";
        
        String i = "[";
        for (Map.Entry<Long, Long> entry : ips) {
            i += "[" + entry.getKey() + "," + entry.getValue() + "],";
        }
        i = i.substring(0,i.length()-1);
        i += "]";
        
        
        String ret = "<script type=\"text/javascript\">\n" +
                    "        var t = " + t + ";\n" +
                    "        var c = " + c + ";\n" +
                    "        var i = " + i + ";\n" +
                    "        $(document).ready(function() {\n" +
                    "            $.plot(\"#flot\", [{data: t, label: 'Tests'},{data: c, label: 'Clients'},{data: i,label:'IPs'}], {\n" +
                    "                        xaxis: {\n" +
                    "                                mode: \"time\",\n" +
                    "                                minTickSize: [1, \"day\"],\n" +
                    "                                timeformat: \"%d.%m.\"\n" +
                    "                        }\n" +
                    "                }); \n" +
                    "        })\n" +
                    "\n" +
                    "                \n" +
                    "\n" +
                    "    </script>";
        
        return ret;
    }
    
    /**
     * Generates the header including bootstrap, jquery, flot and flot.time
     * @return header from doctype to body
     */
    private static String getHeader() {
        return "<!DOCTYPE html>\n" +
                "<html lang=\"en\">\n" +
                "  <head>\n" +
                "    <meta charset=\"utf-8\">\n" +
                "    <style type=\"text/css\">\n" + getCSS() + "</style>\n" + 
                "    <title>Usage Statistics</title>" + 
                "       <!-- jQuery -->\n" +
                "    <script type=\"text/javascript\" src=\"js/jquery-1.8.2.min.js\"></script>\n" +
                "    <!-- FLOT (flotcharts.org -->\n" +
                "    <script type=\"text/javascript\" src=\"js/jquery.flot.min.js\"></script>\n" +
                "    <!-- FLOT time -->\n" +
                "    <script type=\"text/javascript\">\n" +
                "        (function(e){function n(e,t){return t*Math.floor(e/t)}function r(e,t,n,r){if(typeof e.strftime==\"function\")return e.strftime(t);var i=function(e,t){return e=\"\"+e,t=\"\"+(t==null?\"0\":t),e.length==1?t+e:e},s=[],o=!1,u=e.getHours(),a=u<12;n==null&&(n=[\"Jan\",\"Feb\",\"Mar\",\"Apr\",\"May\",\"Jun\",\"Jul\",\"Aug\",\"Sep\",\"Oct\",\"Nov\",\"Dec\"]),r==null&&(r=[\"Sun\",\"Mon\",\"Tue\",\"Wed\",\"Thu\",\"Fri\",\"Sat\"]);var f;u>12?f=u-12:u==0?f=12:f=u;for(var l=0;l<t.length;++l){var c=t.charAt(l);if(o){switch(c){case\"a\":c=\"\"+r[e.getDay()];break;case\"b\":c=\"\"+n[e.getMonth()];break;case\"d\":c=i(e.getDate());break;case\"e\":c=i(e.getDate(),\" \");break;case\"h\":case\"H\":c=i(u);break;case\"I\":c=i(f);break;case\"l\":c=i(f,\" \");break;case\"m\":c=i(e.getMonth()+1);break;case\"M\":c=i(e.getMinutes());break;case\"q\":c=\"\"+(Math.floor(e.getMonth()/3)+1);break;case\"S\":c=i(e.getSeconds());break;case\"y\":c=i(e.getFullYear()%100);break;case\"Y\":c=\"\"+e.getFullYear();break;case\"p\":c=a?\"am\":\"pm\";break;case\"P\":c=a?\"AM\":\"PM\";break;case\"w\":c=\"\"+e.getDay()}s.push(c),o=!1}else c==\"%\"?o=!0:s.push(c)}return s.join(\"\")}function i(e){function t(e,t,n,r){e[t]=function(){return n[r].apply(n,arguments)}}var n={date:e};e.strftime!=undefined&&t(n,\"strftime\",e,\"strftime\"),t(n,\"getTime\",e,\"getTime\"),t(n,\"setTime\",e,\"setTime\");var r=[\"Date\",\"Day\",\"FullYear\",\"Hours\",\"Milliseconds\",\"Minutes\",\"Month\",\"Seconds\"];for(var i=0;i<r.length;i++)t(n,\"get\"+r[i],e,\"getUTC\"+r[i]),t(n,\"set\"+r[i],e,\"setUTC\"+r[i]);return n}function s(e,t){if(t.timezone==\"browser\")return new Date(e);if(!t.timezone||t.timezone==\"utc\")return i(new Date(e));if(typeof timezoneJS!=\"undefined\"&&typeof timezoneJS.Date!=\"undefined\"){var n=new timezoneJS.Date;return n.setTimezone(t.timezone),n.setTime(e),n}return i(new Date(e))}function l(t){t.hooks.processOptions.push(function(t,i){e.each(t.getAxes(),function(e,t){var i=t.options;i.mode==\"time\"&&(t.tickGenerator=function(e){var t=[],r=s(e.min,i),u=0,l=i.tickSize&&i.tickSize[1]===\"quarter\"||i.minTickSize&&i.minTickSize[1]===\"quarter\"?f:a;i.minTickSize!=null&&(typeof i.tickSize==\"number\"?u=i.tickSize:u=i.minTickSize[0]*o[i.minTickSize[1]]);for(var c=0;c<l.length-1;++c)if(e.delta<(l[c][0]*o[l[c][1]]+l[c+1][0]*o[l[c+1][1]])/2&&l[c][0]*o[l[c][1]]>=u)break;var h=l[c][0],p=l[c][1];if(p==\"year\"){if(i.minTickSize!=null&&i.minTickSize[1]==\"year\")h=Math.floor(i.minTickSize[0]);else{var d=Math.pow(10,Math.floor(Math.log(e.delta/o.year)/Math.LN10)),v=e.delta/o.year/d;v<1.5?h=1:v<3?h=2:v<7.5?h=5:h=10,h*=d}h<1&&(h=1)}e.tickSize=i.tickSize||[h,p];var m=e.tickSize[0];p=e.tickSize[1];var g=m*o[p];p==\"second\"?r.setSeconds(n(r.getSeconds(),m)):p==\"minute\"?r.setMinutes(n(r.getMinutes(),m)):p==\"hour\"?r.setHours(n(r.getHours(),m)):p==\"month\"?r.setMonth(n(r.getMonth(),m)):p==\"quarter\"?r.setMonth(3*n(r.getMonth()/3,m)):p==\"year\"&&r.setFullYear(n(r.getFullYear(),m)),r.setMilliseconds(0),g>=o.minute&&r.setSeconds(0),g>=o.hour&&r.setMinutes(0),g>=o.day&&r.setHours(0),g>=o.day*4&&r.setDate(1),g>=o.month*2&&r.setMonth(n(r.getMonth(),3)),g>=o.quarter*2&&r.setMonth(n(r.getMonth(),6)),g>=o.year&&r.setMonth(0);var y=0,b=Number.NaN,w;do{w=b,b=r.getTime(),t.push(b);if(p==\"month\"||p==\"quarter\")if(m<1){r.setDate(1);var E=r.getTime();r.setMonth(r.getMonth()+(p==\"quarter\"?3:1));var S=r.getTime();r.setTime(b+y*o.hour+(S-E)*m),y=r.getHours(),r.setHours(0)}else r.setMonth(r.getMonth()+m*(p==\"quarter\"?3:1));else p==\"year\"?r.setFullYear(r.getFullYear()+m):r.setTime(b+g)}while(b<e.max&&b!=w);return t},t.tickFormatter=function(e,t){var n=s(e,t.options);if(i.timeformat!=null)return r(n,i.timeformat,i.monthNames,i.dayNames);var u=t.options.tickSize&&t.options.tickSize[1]==\"quarter\"||t.options.minTickSize&&t.options.minTickSize[1]==\"quarter\",a=t.tickSize[0]*o[t.tickSize[1]],f=t.max-t.min,l=i.twelveHourClock?\" %p\":\"\",c=i.twelveHourClock?\"%I\":\"%H\",h;a<o.minute?h=c+\":%M:%S\"+l:a<o.day?f<2*o.day?h=c+\":%M\"+l:h=\"%b %d \"+c+\":%M\"+l:a<o.month?h=\"%b %d\":u&&a<o.quarter||!u&&a<o.year?f<o.year?h=\"%b\":h=\"%b %Y\":u&&a<o.year?f<o.year?h=\"Q%q\":h=\"Q%q %Y\":h=\"%Y\";var p=r(n,h,i.monthNames,i.dayNames);return p})})})}var t={xaxis:{timezone:null,timeformat:null,twelveHourClock:!1,monthNames:null}},o={second:1e3,minute:6e4,hour:36e5,day:864e5,month:2592e6,quarter:7776e6,year:525949.2*60*1e3},u=[[1,\"second\"],[2,\"second\"],[5,\"second\"],[10,\"second\"],[30,\"second\"],[1,\"minute\"],[2,\"minute\"],[5,\"minute\"],[10,\"minute\"],[30,\"minute\"],[1,\"hour\"],[2,\"hour\"],[4,\"hour\"],[8,\"hour\"],[12,\"hour\"],[1,\"day\"],[2,\"day\"],[3,\"day\"],[.25,\"month\"],[.5,\"month\"],[1,\"month\"],[2,\"month\"]],a=u.concat([[3,\"month\"],[6,\"month\"],[1,\"year\"]]),f=u.concat([[1,\"quarter\"],[2,\"quarter\"],[1,\"year\"]]);e.plot.plugins.push({init:l,options:t,name:\"time\",version:\"1.0\"}),e.plot.formatDate=r})(jQuery);\n" +
                "    </script>" +
                "   </head>\n" + 
                "   <body>\n" +
                "       <div class=\"container\">" + 
                "       <h1>Usage Statistics</h1>" +
                "       <table class=\"table table-striped table-hover\">";
    }
    
    private static String getFooter() {
        return "</body>\n</html>";
    }
    
    /**
     * Generates a subset of bootstrap css-styling rules
     * @return the css code
     */
    private static String getCSS() {
        //Bootstrap 
        return "/* Bootstrap v2.3.2 *\n"
                + " * Copyright 2012 Twitter, Inc\n"
                + " * Licensed under the Apache License v2.0\n"
                + " * http://www.apache.org/licenses/LICENSE-2.0\n"
                + " *\n"
                + " * Designed and built with all the love in the world @twitter by @mdo and @fat.\n"
                + " */\n"
                + ".container { width: 650px; margin: 0 auto;}\n"
                + "html{font-size:100%;-webkit-text-size-adjust:100%;-ms-text-size-adjust:100%;}\n"
                + "a:focus{outline:thin dotted #333;outline:5px auto -webkit-focus-ring-color;outline-offset:-2px;}\n"
                + "a:hover,a:active{outline:0;}\n"
                + "@media print{*{text-shadow:none !important;color:#000 !important;background:transparent !important;box-shadow:none !important;} a,a:visited{text-decoration:underline;} a[href]:after{content:\" (\" attr(href) \")\";} abbr[title]:after{content:\" (\" attr(title) \")\";} .ir a:after,a[href^=\"javascript:\"]:after,a[href^=\"#\"]:after{content:\"\";} pre,blockquote{border:1px solid #999;page-break-inside:avoid;} thead{display:table-header-group;} tr,img{page-break-inside:avoid;} img{max-width:100% !important;} @page {margin:0.5cm;}p,h2,h3{orphans:3;widows:3;} h2,h3{page-break-after:avoid;}}body{margin:0;font-family:\"Helvetica Neue\",Helvetica,Arial,sans-serif;font-size:14px;line-height:20px;color:#333333;background-color:#ffffff;}\n"
                + "a{color:#0088cc;text-decoration:none;}\n"
                + "a:hover,a:focus{color:#005580;text-decoration:underline;}\n"
                + "table{max-width:100%;background-color:transparent;border-collapse:collapse;border-spacing:0;}\n"
                + ".table{width:100%;margin-bottom:20px;}.table th,.table td{padding:8px;line-height:20px;text-align:left;vertical-align:top;border-top:1px solid #dddddd;}\n"
                + ".table th{font-weight:bold;}\n"
                + ".table thead th{vertical-align:bottom;}\n"
                + ".table caption+thead tr:first-child th,.table caption+thead tr:first-child td,.table colgroup+thead tr:first-child th,.table colgroup+thead tr:first-child td,.table thead:first-child tr:first-child th,.table thead:first-child tr:first-child td{border-top:0;}\n"
                + ".table tbody+tbody{border-top:2px solid #dddddd;}\n"
                + ".table .table{background-color:#ffffff;}\n"
                + ".table-striped tbody>tr:nth-child(odd)>td,.table-striped tbody>tr:nth-child(odd)>th{background-color:#f9f9f9;}\n"
                + ".table-hover tbody tr:hover>td,.table-hover tbody tr:hover>th{background-color:#f5f5f5;}\n"
                + ".table tbody tr.info>td{background-color:#d9edf7;}\n"
                + ".table-hover tbody tr.info:hover>td{background-color:#c4e3f3;}\n"
                + ".r {text-align: right !important;}";
    }
}
