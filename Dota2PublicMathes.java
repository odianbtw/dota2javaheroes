import java.io.IOException;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class Dota2PublicMathes {

    public static void main(String[] args) throws IOException {
        String matchesData = readApi("https://api.opendota.com/api/publicMatches");
        String heroesData = readApi("https://api.opendota.com/api/heroes");

        int[] heroIds = extractNumbersFromArrays(matchesData);
        Map<Integer, String> heroesMap = extractHeroes(heroesData);
        Map<String, Integer> heroPickCount = countHeroPicks(heroIds, heroesMap);


        List<Map.Entry<String, Integer>> topHeroes = getTopHeroes(heroPickCount, 10);


        System.out.println("Most picked heroes in last 100 games:");
        for (Map.Entry<String, Integer> hero : topHeroes) {
            System.out.println(hero.getKey() + ": " + hero.getValue() + " picks");
        }
    }

    public static String readApi(String apiUrl) {
        StringBuilder result = new StringBuilder();
        try (var in = new URL(apiUrl).openStream()) {
            byte[] buffer = new byte[8 * 1024];
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

    public static int[] extractNumbersFromArrays(String jsonString) {
        Pattern pattern = Pattern.compile("\\[(.*?)\\]");
        Matcher matcher = pattern.matcher(jsonString);

        ArrayList<Integer> numbers = new ArrayList<>();

        while (matcher.find()) {
            String arrayContent = matcher.group(1);
            String[] elements = arrayContent.split(",");
            for (String element : elements) {
                try {
                    int number = Integer.parseInt(element.trim());
                    numbers.add(number);
                } catch (NumberFormatException e) {
                    //NOP
                }
            }
        }
        return numbers.stream().mapToInt(Integer::intValue).toArray();
    }

    public static Map<Integer, String> extractHeroes(String jsonString) {
        Map<Integer, String> heroesMap = new HashMap<>();

        String[] heroEntries = jsonString.split("\\},\\{");

        for (String entry : heroEntries) {
            String idStr = entry.replaceAll(".*\"id\":", "").replaceAll(",.*", "").trim();
            String nameStr = entry.replaceAll(".*\"localized_name\":\"", "").replaceAll("\".*", "").trim();

            int id = Integer.parseInt(idStr);
            heroesMap.put(id, nameStr);
        }
        return heroesMap;
    }

    public static Map<String, Integer> countHeroPicks(int[] heroIds, Map<Integer, String> heroesMap) {
        Map<String, Integer> pickCount = new HashMap<>();
        for (int id : heroIds) {
            if (heroesMap.containsKey(id)) {
                String heroName = heroesMap.get(id);
                pickCount.put(heroName, pickCount.getOrDefault(heroName, 0) + 1);
            }
        }
        return pickCount;
    }

    public static List<Map.Entry<String, Integer>> getTopHeroes(Map<String, Integer> pickCount, int topN) {
        List<Map.Entry<String, Integer>> list = new ArrayList<>(pickCount.entrySet());
        list.sort((entry1, entry2) -> entry2.getValue().compareTo(entry1.getValue()));
        return list.subList(0, Math.min(topN, list.size()));
    }

}
