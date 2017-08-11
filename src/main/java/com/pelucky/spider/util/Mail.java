package com.pelucky.spider.util;

import java.io.UnsupportedEncodingException;
import java.util.Date;
import java.util.List;
import java.util.Properties;

import javax.activation.DataHandler;
import javax.activation.FileDataSource;
import javax.mail.Address;
import javax.mail.BodyPart;
import javax.mail.Message;
import javax.mail.Multipart;
import javax.mail.Session;
import javax.mail.Transport;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeBodyPart;
import javax.mail.internet.MimeMessage;
import javax.mail.internet.MimeMultipart;
import javax.mail.internet.MimeUtility;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class Mail {
    private Properties properties;
    private final String SSL_FACTORY = "javax.net.ssl.SSLSocketFactory";
    private final String port = "465"; // 端口
    private String smtpServer; // SMTP服务器地址
    private String username; // 登录SMTP服务器的用户名
    private String password; // 登录SMTP服务器的密码
    private Logger logger = LoggerFactory.getLogger(Mail.class);

    public Mail(String smtpServer, String username, String password) {
        properties = new Properties();
        this.smtpServer = smtpServer;
        this.username = username;
        this.password = password;
    }

    /**
     * 进行base64加密，防止中文乱码
     */
    private String changeEncode(String str) {
        try {
            str = MimeUtility.encodeText(new String(str.getBytes(), "UTF-8"), "UTF-8", "B"); // "B"代表Base64
        } catch (UnsupportedEncodingException e) {
            logger.info("{} UnsupportedEncodingException", str);
            e.printStackTrace();
        }
        System.out.println("After base64 encode subject: " + str);
        return str;
    }

    private void setEnvironmental() {
        properties.put("mail.smtp.host", smtpServer);
        properties.put("mail.smtp.auth", "true");
        properties.put("mail.smtp.socketFactory.class", SSL_FACTORY); // 使用JSSE的SSL
                                                                      // socketfactory来取代默认的socketfactory
        properties.put("mail.smtp.socketFactory.fallback", "false"); // 只处理SSL的连接,对于非SSL的连接不做处理
        properties.put("mail.smtp.port", port);
        properties.put("mail.smtp.socketFactory.port", port);
    }

    private String getMailList(String[] mailArray) {
        StringBuffer toList = new StringBuffer();
        int length = mailArray.length;
        if (mailArray != null && length < 2) {
            toList.append(mailArray[0]);
        } else {
            for (int i = 0; i < length; i++) {
                toList.append(mailArray[i]);
                if (i != (length - 1)) {
                    toList.append(",");
                }

            }
        }
        return toList.toString();
    }

    /**
     * 正式发邮件
     */
    public boolean sendMail(String[] recipients, String subject, String content, List<String> attachmentNames) {
        setEnvironmental();
        Session session = Session.getInstance(properties);
        session.setDebug(false);
        MimeMessage message = new MimeMessage(session);

        try {
            // 发件人
            Address address = new InternetAddress(username);
            message.setFrom(address);

            // 收件人
            // /**
            // * TO：代表有健的主要接收者。 CC：代表有健的抄送接收者。 BCC：代表邮件的暗送接收者。
            // */
            // }
            String toList = getMailList(recipients);
            @SuppressWarnings("static-access")
            InternetAddress[] iaToList = new InternetAddress().parse(toList);
            message.setRecipients(Message.RecipientType.TO, iaToList);

            // 主题
            //message.setSubject(changeEncode(subject));
            message.setSubject(subject);

            // 时间
            message.setSentDate(new Date());

            Multipart multipart = new MimeMultipart();
            // 添加文本
            BodyPart text = new MimeBodyPart();
            text.setContent(content, "text/html; charset=utf-8");
            multipart.addBodyPart(text);
            // 添加附件
            for (String fileName : attachmentNames) {
                BodyPart adjunct = new MimeBodyPart();
                FileDataSource fileDataSource = new FileDataSource(fileName);
                adjunct.setDataHandler(new DataHandler(fileDataSource));
                adjunct.setFileName(changeEncode(fileDataSource.getName()));
                multipart.addBodyPart(adjunct);
            }
            // 清空附件集合
            attachmentNames.clear();

            message.setContent(multipart, "text/html; charset=utf-8");
            message.saveChanges();

        } catch (Exception e) {
            logger.info("Get email element fail..");
            e.printStackTrace();
            return false;
        }

        try {
            Transport transport = session.getTransport("smtp");
            transport.connect(smtpServer, username, password);
            transport.sendMessage(message, message.getAllRecipients());
            transport.close();
        } catch (Exception e) {
            logger.info("Send email fail..");
            e.printStackTrace();
            return false;
        }
        logger.info("Send email succesfully!");
        return true;
    }
}
