# Read Wechat Subscription in email

## What can it do
- It can collect wechat subscription from [chuansongmen](http://chuansong.me), and send to the email with each content's title and url!
- So you can get rid of your phone and save more times!
- Thanks to chuansong.me

## Environment
- You should have at lesast Java 1.8 and Maven 3.3.9
- Linux is perfered, Windows is also OK

## Usage
- Before package, you should rename /src/main/resources/config.properties.sample to config.properties and change config in it

```
mvn package
cd target
Linux: sh run.sh start
Windows: java -cp wxSubscriptionSpider-0.0.1-SNAPSHOT.jar;.\conf com.pelucky.spider.SpiderApp
```

- Or you can install it in Raspberry PI and crontab to run it


## Configuration

```
url=http://chuansong.me
wxSubscription=linux-cn, wow36kr,	    # Subscription's name
lastDays=7  				    # Collect the latest days
toMail=xxxxxx@163.com, xxxxxxxx@qq.com      # Send to the receive email
fromMail=xxxxxx@qq.com			    # From which mail to send
hostMail=smtp.qq.com			    # smtp's server of your e-mail
password=xxxxxxxxxxxxxxxxxxx		    # The email's password
```
