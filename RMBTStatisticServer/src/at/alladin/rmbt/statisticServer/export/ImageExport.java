/*******************************************************************************
 * Copyright 2013-2016 RTR-GmbH
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
package at.alladin.rmbt.statisticServer.export;

import java.awt.Color;
import java.awt.Font;
import java.awt.FontMetrics;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.io.OutputStream;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Types;
import java.text.FieldPosition;
import java.text.Format;
import java.util.Locale;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.imageio.ImageIO;

import org.restlet.data.MediaType;
import org.restlet.data.Status;
import org.restlet.representation.OutputRepresentation;
import org.restlet.representation.Representation;
import org.restlet.representation.StringRepresentation;
import org.restlet.resource.Get;

import at.alladin.rmbt.shared.SignificantFormat;
import at.alladin.rmbt.statisticServer.ServerResource;

/**
 *
 * @author ths
 */
public class ImageExport extends ServerResource {

    @Get
    public Representation retrieve(final String entity) {
        if (!getRequest().getAttributes().containsKey("lang") || !getRequest().getAttributes().containsKey("size")) {
            setStatus(Status.CLIENT_ERROR_BAD_REQUEST);
            return new StringRepresentation("illegal parameters");
        }
                
        if (!getRequest().getAttributes().containsKey("open_test_uuid")) {
            return new StringRepresentation("invalid uuid");
        }
        final String uuid = getRequest().getAttributes().get("open_test_uuid").toString().substring(1); //since the first letter is a 'O'
        final String lang = getRequest().getAttributes().get("lang").toString();
        final String size = getRequest().getAttributes().get("size").toString();
        
        if (!lang.equals("de") && !lang.equals("en")) {
            setStatus(Status.CLIENT_ERROR_BAD_REQUEST);
            return new StringRepresentation("invalid language");
        }
        
        if (!size.equals("forumlarge") && !size.equals("forumsmall") && !size.equals("thumbnail")) {
            setStatus(Status.CLIENT_ERROR_BAD_REQUEST);
            return new StringRepresentation("invalid image type");
        }

        //first - get data
        final String sql = "SELECT"
                //+ " ('P' || t.open_uuid) open_uuid,"
                //+ " to_char(t.time AT TIME ZONE 'UTC', 'YYYY-MM-DD HH24:MI') \"time\","
                + " nt.name network_type,"
                //+ " t.geo_provider loc_src,"
                //+ " t.zip_code,"
                + " t.speed_download download_kbit,"
                + " t.speed_upload upload_kbit,"
                + " (t.ping_median::float / 1000000) ping_ms,"
                + " t.signal_strength,"
                + " COALESCE(prov.shortname, mprov.shortname, prov.name, mprov.name, network_operator_name, network_sim_operator) provider_name,"
                + " COALESCE(t.plattform, t.client_name) as platform "
                //+ " network_operator network_mcc_mnc,"
                //+ " network_operator_name network_name,"
                //+ " network_sim_operator sim_mcc_mnc,"
                //+ " nat_type \"connection\","
                //+ " public_ip_asn asn,"
                //+ " client_public_ip_anonymized ip_anonym,"
                //+ " (ndt.s2cspd*1000)::int ndt_download_kbit,"
                //+ " (ndt.c2sspd*1000)::int ndt_upload_kbit"
                + " FROM test t"
                + " LEFT JOIN network_type nt ON nt.uid=t.network_type"
                + " LEFT JOIN device_map adm ON adm.codename=t.model"
                + " LEFT JOIN test_server ts ON ts.uid=t.server_id"
                + " LEFT JOIN test_ndt ndt ON t.uid=ndt.test_id"
                + " LEFT JOIN provider prov ON mobile_provider_id = prov.uid "
                + " LEFT JOIN provider mprov ON provider_id = mprov.uid"
                + " WHERE "
                + " t.deleted = false AND t.implausible = false"
                + " AND status = 'FINISHED'"
                + " AND open_test_uuid = ?";

        PreparedStatement ps = null;
        ResultSet rs = null;
        try {
            ps = conn.prepareStatement(sql);
            ps.setObject(1, uuid, Types.OTHER);
            rs = ps.executeQuery();
            if (!rs.next()) {
                setStatus(Status.CLIENT_ERROR_NOT_FOUND);
                return new StringRepresentation("invalid uuid");
            }

            final double download = ((double) rs.getInt("download_kbit")) / 1000;
            final double upload = ((double) rs.getInt("upload_kbit")) / 1000;
            final double ping = rs.getFloat("ping_ms");
            final String isp = rs.getString("provider_name");
            final String typ = rs.getString("network_type");
            final String signal = rs.getString("signal_strength");
            final String os = rs.getString("platform");

            final OutputRepresentation result = new OutputRepresentation(MediaType.IMAGE_PNG) {
                @Override
                public void write(OutputStream out) throws IOException {

                    ShareImageGenerator generator;
                    if (size.equals("thumbnail")) {
                        generator = new FacebookThumbnailGenerator();
                    }
                    else if (size.equals("forumsmall")){
                        generator = new ForumBannerSmallGenerator();
                    } else {
                        generator = new ForumBannerGenerator();
                    }
                    
                    BufferedImage img = generator.generateImage(lang, upload, download, ping, isp, typ, signal, os);
                    


                    ImageIO.write(img, "png", out);
                }


            };
            return result;

        } catch (SQLException e) {
            Logger.getLogger(ImageExport.class.getName()).log(Level.SEVERE, null, e);
            setStatus(Status.CLIENT_ERROR_NOT_FOUND);
            return new StringRepresentation("invalid uuid");
        }






    }
    
     
    public abstract class ShareImageGenerator {
        /**
         * Generates a image for showing the user its speed test result
         * @param lang: Language of the image, currently either 'de' or 'en'
         * @param upload: Upload speed in mbps
         * @param download: Download speed in mbps
         * @param ping: Ping in ms
         * @param isp: ISP name
         * @param typ: Test type (LAN, 3G, 4G, etc.)
         * @param signal: Signal strength in dbm
         * @param os: Plattform used for conducting the test (Android, IOS, Applet, Browser)
         * @return the image OR null, if parameters are incorrect
         */
        public abstract BufferedImage generateImage(String lang, double upload, double download, double ping, String isp, String typ, String signal, String os) throws IOException;
        
        /**
         * Formats a number to 2 significant digits
         * @param number the number
         * @return the formatted number
         */
        protected String formatNumber(double number, String lang) {
        	final Locale locale = new Locale(lang);
        	final Format format = new SignificantFormat(2, locale);
        	
        	final StringBuffer buf = format.format(number, new StringBuffer(), new FieldPosition(0));
        	return buf.toString();
        }

       protected void drawCenteredString(String s, int x, int y, int w, int h, Graphics g) {
            FontMetrics fm = g.getFontMetrics();
            x += (w - fm.stringWidth(s)) / 2;
            y += (fm.getAscent() + (h - (fm.getAscent() + fm.getDescent())) / 2);
            g.drawString(s, x, y);
       }
    }
    
    public class ForumBannerGenerator extends ShareImageGenerator {

        @Override
        public BufferedImage generateImage(String lang, double upload, double download, double ping, String isp, String typ, String signal, String os) throws IOException{            
            String unknownString = (lang.equals("de")) ? "unbekannt" : "unknown";
            BufferedImage img = new BufferedImage(600, 200, BufferedImage.TYPE_INT_ARGB);  
            img.createGraphics();  
            Graphics2D g = (Graphics2D)img.getGraphics(); 
            g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

            BufferedImage img2 = ImageIO.read(getClass().getResourceAsStream("forumbanner_" + lang + ".png"));
            g.drawImage(img2, null, 0, 0);

            //Speeds
            g.setColor(Color.black);
            g.setFont(new Font("Droid Sans", Font.BOLD, 60));
            g.drawString(formatNumber(download, lang), 30, 105);
            g.drawString(formatNumber(upload, lang), 230, 105);
            g.drawString(formatNumber(ping, lang), 440, 105);


            //ISP and other information
            g.setColor(Color.WHITE);
            g.setFont(new Font("Verdana", Font.BOLD,16));

            //de
            if (lang.equals("de")) {
                //left
                g.drawString((typ == null)?unknownString:typ, 110, 168);
                g.drawString((isp == null)?unknownString:isp, 110, 191);

                //right
                g.drawString((signal==null)?unknownString:signal + " dBm", 410, 168);
                g.drawString((os==null)?unknownString:os, 410, 191);
            } 
            else { //en
                //left
                g.drawString((typ == null)?unknownString:typ, 130, 168);
                g.drawString((isp == null)?unknownString:isp, 90, 191);

                //right
                g.drawString((signal==null)?unknownString:signal + " dBm", 445, 168);
                g.drawString((os==null)?unknownString:os, 445, 191);
            }
            
            return img;
        }
        
    }
    
     public class ForumBannerSmallGenerator extends ShareImageGenerator {

        @Override
        public BufferedImage generateImage(String lang, double upload, double download, double ping, String isp, String typ, String signal, String os) throws IOException {
            String unknownString = (lang.equals("de")) ? "unbekannt" : "unknown";
            BufferedImage img = new BufferedImage(390, 130, BufferedImage.TYPE_INT_ARGB);  
            img.createGraphics();  
            Graphics2D g = (Graphics2D)img.getGraphics(); 
            g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

            BufferedImage img2 = ImageIO.read(getClass().getResourceAsStream("forumsmall_" + lang + ".png"));
            g.drawImage(img2, null, 0, 0);
            
            //Speeds
            g.setColor(Color.black);
            g.setFont(new Font("Droid Sans", Font.BOLD, 40));
            drawCenteredString(formatNumber(download, lang), 0, 20,130,65,g);
            drawCenteredString(formatNumber(upload, lang), 130, 20,130,65,g);
            drawCenteredString(formatNumber(ping, lang), 260, 20,130,65,g);


            //ISP and other information
            g.setColor(Color.WHITE);
            g.setFont(new Font("Verdana", Font.BOLD,10));

            //de
            if (lang.equals("de")) {
                //left
                g.drawString((typ == null)?unknownString:typ, 73, 109);
                g.drawString((isp == null)?unknownString:isp, 73, 124);

                //right
                g.drawString((signal==null)?"":signal + " dBm", 270, 109);
                g.drawString((os==null)?unknownString:os, 270, 124);
                
                //hide signal caption if signal is null
                if (signal==null) {
                	g.setColor(new Color(89,178,0));
                	g.fillRect(195, 98, 71, 13);
                	
                }
                
            } 
            else { //en
                //left
                g.drawString((typ == null)?unknownString:typ, 83, 109);
                g.drawString((isp == null)?unknownString:isp, 60, 124);

                //right
                g.drawString((signal==null)?"":signal + " dBm", 290, 109);
                g.drawString((os==null)?unknownString:os, 290, 124);
                
                //hide signal caption if signal is null
                if (signal==null) {
                	g.setColor(new Color(89,178,0));
                	g.fillRect(195, 98, 90, 13);
                	
                }
            }
            
            g.dispose();
            
            return img;
        }

    
    }

    public class FacebookThumbnailGenerator extends ShareImageGenerator {

        @Override
        public BufferedImage generateImage(String lang, double upload, double download, double ping, String isp, String typ, String signal, String os) throws IOException {
            
            BufferedImage img = new BufferedImage(100, 100, BufferedImage.TYPE_INT_ARGB);  
            img.createGraphics();  
            Graphics2D g = (Graphics2D)img.getGraphics(); 
            g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

            BufferedImage img2 = ImageIO.read(getClass().getResourceAsStream("netztest-thumbnail.png"));
            g.drawImage(img2, null, 0, 0);

            //Speeds
            g.setColor(Color.white);
            g.setFont(new Font("Droid Sans", Font.PLAIN, 35));
            String up = formatNumber(upload, lang);
            drawCenteredString(up, 25, 48, 80, 54, g);
            String down = formatNumber(download, lang);
            drawCenteredString(down, 0, 0, 80, 54, g);
            
            g.dispose();
            return img;
        }
        
    }
}
