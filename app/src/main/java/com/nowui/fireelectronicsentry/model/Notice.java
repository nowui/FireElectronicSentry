package com.nowui.fireelectronicsentry.model;

/**
 * Created by yongqiangzhong on 8/2/15.
 */
public class Notice {

    private Integer messageid;

    private String messageTitle;

    private String messageContent;

    private String messagePictureBase64;

    private String messageAudioBase64;

    private String messageHasReaded;

    public String getMessageContent() {
        return messageContent;
    }

    public void setMessageContent(String messageContent) {
        this.messageContent = messageContent;
    }

    public Integer getMessageid() {
        return messageid;
    }

    public void setMessageid(Integer messageid) {
        this.messageid = messageid;
    }

    public String getMessageTitle() {
        return messageTitle;
    }

    public void setMessageTitle(String messageTitle) {
        this.messageTitle = messageTitle;
    }

    public String getMessagePictureBase64() {
        return messagePictureBase64;
    }

    public void setMessagePictureBase64(String messagePictureBase64) {
        this.messagePictureBase64 = messagePictureBase64;
    }

    public String getMessageAudioBase64() {
        return messageAudioBase64;
    }

    public void setMessageAudioBase64(String messageAudioBase64) {
        this.messageAudioBase64 = messageAudioBase64;
    }

    public String getMessageHasReaded() {
        return messageHasReaded;
    }

    public void setMessageHasReaded(String messageHasReaded) {
        this.messageHasReaded = messageHasReaded;
    }
}
