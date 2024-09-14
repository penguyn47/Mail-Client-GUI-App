package src;

import src.entity.Mail;
import src.entity.User;
import src.gui.GUI;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.List;
import java.util.Map;
import java.util.ArrayList;
import java.util.HashMap;

import src.config.Config;
import src.config.FilterConfig;
import src.entity.Folder;

public class EmailController {
    EmailService emailService;
    Config config;
    public List<Folder> folders;
    public User user;
    GUI gui;
    EmailController(Config config){
        this.config = config;
        createEmailService();
        createFolders();
        createUser();

        loadAllMails();
        loadMailsWithFilters();

        gui = new GUI(this);
    }

    private void createEmailService(){
        emailService = new EmailService(
            config.getServerConfig().getSmtpHost(),
            config.getServerConfig().getSmtpPort(),
            config.getServerConfig().getPop3Host(),
            config.getServerConfig().getPop3Port()
        );
    }

    private void createFolders(){
        folders = new ArrayList<>();
        // Default folders
        folders.add(new Folder("All"));
        folders.add(new Folder("Inbox"));
        folders.add(new Folder("Project"));
        folders.add(new Folder("Important"));
        folders.add(new Folder("Work"));
        folders.add(new Folder("Spam"));
    }

    private void createUser(){
        user = new User(
            config.getClientConfig().getUsername(),
            config.getClientConfig().getPassword(),
            config.getClientConfig().getEmail()
        );
    }

    public Folder getFolder(String name){
        for(Folder f : folders){
            if(f.getName() == name) return f;
        }
        return null;
    }

    private void saveMailsStatus(){
        String filePath = ".client\\" + user.username + "\\readMails.txt";
        try (FileWriter writer = new FileWriter(filePath)) {
            for(Mail mail : folders.get(0).getMails()){
                if(mail.isRead()) {
                    writer.write(mail.getId() + "\n");
                }
            }
            writer.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void loadAllMails(){
        int n = (emailService.getMailsCount(user.email, user.encodedPassword));
        for(int i=1;i<=n;i++){
            folders.get(0).addMail(
                emailService.retrieveMail(user.email, user.encodedPassword, i)
            );
        }

        String filePath = ".client\\" + user.username + "\\readMails.txt";
        new File(filePath).getParentFile().mkdirs();
        try {
            new File(filePath).createNewFile();
        } catch (IOException e) {
            System.out.println(e.getMessage());
        }

        folders.get(0).loadMailsStatusFromFile(filePath);
    }

    public void sendMail(Mail mail){
        emailService.sendMail(mail); // i dont know :D
    }

    private void loadMailsWithFilters(){
        Map<String, Folder> folderMap = new HashMap<>();
        for(Folder folder : folders){
            folderMap.put(folder.getName(), folder);
        }

        for(Mail mail : folders.get(0).getMails()){
            for(FilterConfig filterConfig : config.getFilterConfigs()){
                if(filterConfig.emails.contains(mail.getFrom())){
                    folderMap.get(filterConfig.desFolder).addMail(mail);
                    continue;
                }
                boolean isContainsd = false;
                for(String token : filterConfig.subjects){
                    if(mail.getSubject().contains(token)) {
                        isContainsd = true;
                        break;
                    }
                }
                for(String token : filterConfig.contents){
                    if(mail.getBody().contains(token)){
                        isContainsd = true;
                        break;
                    }
                }
                if(isContainsd) folderMap.get(filterConfig.desFolder).addMail(mail);
            }
        }
    }

    public void downloadFiles(int iMail, int[] indices, String desPath){
        if(indices == null){
            emailService.downloadAttachments(user.email, user.encodedPassword, iMail, desPath);
        } else {
            emailService.downloadAttachment(user.email, user.encodedPassword, iMail, desPath, indices);
        }
    }
}
