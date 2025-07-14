package com.example.daltutor.core;

public class Posting {
    String topic;
    String address;
    String description;
    String duration;
    String fee;
    String tutor;
    String datetime;

    public Posting(String topic, String address, String description, String duration, String fee,
                   String tutor, String datetime) {
        this.topic = topic;
        this.address = address;
        this.description = description;
        this.duration = duration;
        this.fee = fee;
        this.tutor = tutor;
        this.datetime = datetime;
    }

    public String getTopic() {
        return topic;
    }

    public String getAddress() {
        return address;
    }

    public String getDescription() {
        return description;
    }

    public String getDuration() {
        return duration;
    }

    public String getFee() {
        return fee;
    }

    public String getTutor() {
        return tutor;
    }

    public String getDatetime() {
        return datetime;
    }

    public boolean matchesCriteria(int feeMin, int feeMax, int durMin, int durMax, String topic) {
        boolean feeMatch = (feeMin <= Integer.parseInt(fee)) && (feeMax >= Integer.parseInt(fee));
        boolean durMatch = (durMin <= Integer.parseInt(duration)) && (durMax >= Integer.parseInt(fee));
        boolean topicMatch = topic.equals(this.topic) || topic.equals("Any");
        return feeMatch && durMatch && topicMatch;
    }
}
