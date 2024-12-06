/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 */

package com.mycompany.simplesearchtool;

/**
 *
 * @author bernardoleal
 */
public class SearchEngine {
    
    private int id;
    private String content;
    public SearchEngine(int id, String content) {
        this.id = id;
        this.content = content;
    }
    public int getId(){
        return id;
    }
    
    public String getContent(){
        return content;
    }

    public static void main(String[] args) {
        System.out.println("Hello World!");
    }
}
