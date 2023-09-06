package com.example.myapplication.Utils;

import com.example.myapplication.DataModel.CarService;
import com.example.myapplication.DataModel.Service;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class ServiceSearchFilter {

    public static ArrayList<CarService> filterListofServices(List<CarService> services, String text) {
        ArrayList<CarService> filteredlist=new ArrayList<>();
        System.out.println();
        for (CarService item : services) {
            System.out.println("SERVICE NAME:"+item.getServiceName());
            System.out.println("TEXT" +text);
            if (item.getServiceName().toLowerCase().contains(text.toLowerCase())) {
                filteredlist.add(item);
            }
        }

        Collections.sort(services, (s1, s2) -> {
            int matchCount1 = countMatchingCharacters(s1.getServiceName(), text.toLowerCase());
            int matchCount2 = countMatchingCharacters(s2.getServiceName(), text.toLowerCase());

            if (matchCount1 == matchCount2) {
                int distance1 = getLevenshteinDistance(s1.getServiceName(), text.toLowerCase());
                int distance2 = getLevenshteinDistance(s2.getServiceName(), text.toLowerCase());
                return Integer.compare(distance1, distance2);
            }

            return Integer.compare(matchCount2, matchCount1);
        });

        return filteredlist;
    }
    public static int countMatchingCharacters(String s1, String s2) {
        int count = 0;
        int minLength = Math.min(s1.length(), s2.length());

        for (int i = 0; i < minLength; i++) {
            if (s1.charAt(i) == s2.charAt(i)) {
                count++;
            } else {
                break;
            }
        }
        if (count==0){
            return -1;
        }
        return count;
    }
    public  static int getLevenshteinDistance(String s1, String s2) {
        int[][] dp = new int[s1.length() + 1][s2.length() + 1];

        for (int i = 0; i <= s1.length(); i++) {
            dp[i][0] = i;
        }

        for (int j = 0; j <= s2.length(); j++) {
            dp[0][j] = j;
        }

        for (int i = 1; i <= s1.length(); i++) {
            for (int j = 1; j <= s2.length(); j++) {
                int cost = s1.charAt(i - 1) == s2.charAt(j - 1) ? 0 : 1;
                dp[i][j] = Math.min(dp[i - 1][j] + 1, Math.min(dp[i][j - 1] + 1, dp[i - 1][j - 1] + cost));
            }
        }

        return dp[s1.length()][s2.length()];
    }
}
