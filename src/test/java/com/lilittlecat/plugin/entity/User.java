package com.lilittlecat.plugin.entity;

import java.util.List;

/**
 * @author LiLittleCat
 * @since 4/13/2022
 */
public class User {
    private String name;
    private Integer age;
    private List<String> nickNames;
//    private List<T> things;

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public Integer getAge() {
        return age;
    }

    public void setAge(Integer age) {
        this.age = age;
    }

    public List<String> getNickNames() {
        return nickNames;
    }

    public void setNickNames(List<String> nickNames) {
        this.nickNames = nickNames;
    }

//    public List<T> getThings() {
//        return things;
//    }
//
//    public void setThings(List<T> things) {
//        this.things = things;
//    }

    public static void main(String[] args) {
        User user = new User();

    }
}
