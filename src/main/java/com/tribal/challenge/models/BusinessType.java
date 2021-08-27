package com.tribal.challenge.models;

public enum BusinessType {
    STARTUP("startup"),
    SME("SME");

    String name;

    BusinessType(String name) {
        this.name = name;
    }

    public static boolean isValid(String businessType){
        for(var bt: BusinessType.values()){
            if(bt.name.equalsIgnoreCase(businessType)){
                return true;
            }
        }

        return  false;
    }

    public static BusinessType of(String businessType){
        for(var bt: BusinessType.values()){
            if(bt.name.equalsIgnoreCase(businessType)){
                return bt;
            }
        }

        throw new RuntimeException("Not valid BusinessType");
    }
}
