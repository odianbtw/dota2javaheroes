import java.io.IOException;
import java.net.URL;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class Dota2ProMathces {

    public static void main(String[] args) {
        String proMathes = readApi("https://api.opendota.com/api/proMatches");
        String heroesData = readApi("https://api.opendota.com/api/heroes");
        String[] matchIds = extractMatchIds(proMathes);
        String[] shortMatchIds  = Arrays.copyOf(matchIds, matchIds.length / 2);
        ArrayList<Integer> heroIDs = new ArrayList<>();
        for (String matchID : shortMatchIds){
            String matchInfo = readApi("https://api.opendota.com/api/matches/" + matchID);
            extractHeroIdsFromMatch(matchInfo, heroIDs);
            try {
                Thread.sleep(250);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
        int[] heroes = heroIDs.stream().mapToInt(Integer::intValue).toArray();
        Map<Integer, String> heroesMap = Dota2PublicMathes.extractHeroes(heroesData);
        Map<String, Integer> heroPickCount = Dota2PublicMathes.countHeroPicks(heroes, heroesMap);
        List<Map.Entry<String, Integer>> topHeroes = Dota2PublicMathes.getTopHeroes(heroPickCount, 10);
        System.out.println("Most picked heroes in last 50 Pro games:");
        for (Map.Entry<String, Integer> hero : topHeroes) {
            System.out.println(hero.getKey() + ": " + hero.getValue() + " picks");
        }
    }




    public static void extractHeroIdsFromMatch(String matchJson, ArrayList<Integer> heroIDs) {

        Pattern pattern = Pattern.compile("\"draft_timings\":\\s*\\[(.*?)\\]");
        Matcher matcher = pattern.matcher(matchJson);

        if (matcher.find()) {

            String draftTimingsArray = matcher.group(1);

            Pattern heroPattern = Pattern.compile("\\{.*?\"pick\":\\s*true.*?\"hero_id\":\\s*([0-9]+).*?\\}");
            Matcher heroMatcher = heroPattern.matcher(draftTimingsArray);

            while (heroMatcher.find()) {
                heroIDs.add(Integer.parseInt(heroMatcher.group(1)));
            }
        }
    }

    public static String readApi(String apiUrl) {
        StringBuilder result = new StringBuilder();
        try (var in = new URL(apiUrl).openStream()) {
            byte[] buffer = new byte[128 * 1024];
            int val;

            while ((val = in.read(buffer)) != -1) {
                result.append(new String(buffer, 0, val));
            }
        } catch (IOException e) {
            e.printStackTrace();
            System.exit(1);
        }
        return result.toString();
    }

    public static String[] extractMatchIds(String jsonString) {
        Pattern pattern = Pattern.compile("\"match_id\":\\s*([0-9]+)");
        Matcher matcher = pattern.matcher(jsonString);

        List<String> ids = new ArrayList<>();

        while (matcher.find()) {
            ids.add(matcher.group(1));
        }

        return ids.toArray(new String[0]);
    }

}
