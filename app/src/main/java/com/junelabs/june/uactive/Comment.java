package com.junelabs.june.uactive;

/**
 * Created by June on 3/12/2016.
 */
public class Comment {
    private int commentID;
    private String text;
    private String user;

    public Comment(int commentID, String text, String user){

        this.commentID = commentID;
        this.text = text;
        this.user = user;
    }

    public Comment(String parseCode){

        String[] params = parseCode.split(",");

        this.commentID = Integer.valueOf(params[0]);
        this.text = params[1];
        this.user = params[2];
    }

    public int getCommentID() {
        return commentID;
    }

    public void setCommentID(int commentID) {
        this.commentID = commentID;
    }

    public String getText() {
        return text;
    }

    public void setText(String text) {
        this.text = text;
    }

    public String getUser() {
        return user;
    }

    public void setUser(String user) {
        this.user = user;
    }
}
