package fermi;

import pulsar_information.Information;


public class SubInformation {
    public static Information getIth(final Information info, int total, int ith) {
        return Utils.getIth(info, total, ith);
    }

    /*public static void main(String[] args){
        Information info = new Information();
        info.setEmin(100);
        info.setEmax(300000);
        Information res = getIth(info, 8, 5);
        System.out.println(res.getEmin());
        System.out.println(res.getEmax());
    }*/
}











