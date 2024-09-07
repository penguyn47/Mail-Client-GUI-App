
public class EmailController {
    EmailService emailService;
    EmailController(String smtpHost, int smtpPort, String pop3Host, int pop3Port){
        emailService = new EmailService(smtpHost, smtpPort, pop3Host, pop3Port);

        Mail mail = new Mail();
        mail.setFrom("from.com");
        mail.addTo("to1.com");
        mail.addTo("to2.com");
        mail.setSubject("send mail test 2");
        mail.setBody("1234567890123456789012345678901234567890");
        
        emailService.sendEmail(mail);
    }

    
}
