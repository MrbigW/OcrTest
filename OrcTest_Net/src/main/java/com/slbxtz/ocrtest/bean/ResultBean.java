package com.slbxtz.ocrtest.bean;

import java.util.List;

/**
 * Created by MrbigW on 2017/1/9.
 * weChat:1024057635
 * GitHub:MrbigW
 * Usage: -.-
 * -------------------=.=------------------------
 */

public class ResultBean {


    /**
     * image_id : bwb5joXIejZnuTblZa0EKA==
     * request_id : 1483941006,3db220ae-cbda-4e3f-909a-b5cb234081a3
     * cards : [{"gender":"男","name":"王瑞康","id_card_number":"610302199401160018","birthday":"1994-01-16","race":"汉","address":"西安市雁塔区雁塔路南段二号B区公寓2012号","type":1,"side":"front"}]
     * time_used : 1179
     */

    private String image_id;
    private String request_id;
    private int time_used;
    private List<CardsBean> cards;

    public String getImage_id() {
        return image_id;
    }

    public void setImage_id(String image_id) {
        this.image_id = image_id;
    }

    public String getRequest_id() {
        return request_id;
    }

    public void setRequest_id(String request_id) {
        this.request_id = request_id;
    }

    public int getTime_used() {
        return time_used;
    }

    public void setTime_used(int time_used) {
        this.time_used = time_used;
    }

    public List<CardsBean> getCards() {
        return cards;
    }

    public void setCards(List<CardsBean> cards) {
        this.cards = cards;
    }

    public static class CardsBean {
        /**
         * gender : 男
         * name : 王瑞康
         * id_card_number : 610302199401160018
         * birthday : 1994-01-16
         * race : 汉
         * address : 西安市雁塔区雁塔路南段二号B区公寓2012号
         * type : 1
         * side : front
         */

        private String gender;
        private String name;
        private String id_card_number;
        private String birthday;
        private String race;
        private String address;
        private int type;
        private String side;

        public String getGender() {
            return gender;
        }

        public void setGender(String gender) {
            this.gender = gender;
        }

        public String getName() {
            return name;
        }

        public void setName(String name) {
            this.name = name;
        }

        public String getId_card_number() {
            return id_card_number;
        }

        public void setId_card_number(String id_card_number) {
            this.id_card_number = id_card_number;
        }

        public String getBirthday() {
            return birthday;
        }

        public void setBirthday(String birthday) {
            this.birthday = birthday;
        }

        public String getRace() {
            return race;
        }

        public void setRace(String race) {
            this.race = race;
        }

        public String getAddress() {
            return address;
        }

        public void setAddress(String address) {
            this.address = address;
        }

        public int getType() {
            return type;
        }

        public void setType(int type) {
            this.type = type;
        }

        public String getSide() {
            return side;
        }

        public void setSide(String side) {
            this.side = side;
        }
    }
}
