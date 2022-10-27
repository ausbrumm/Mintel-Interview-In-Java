import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;

import java.util.*;
import java.util.stream.Collectors;

import org.javatuples.Pair;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;

import javax.sound.midi.Soundbank;

@SuppressWarnings("unchecked")
public class Main {
    public static void main(String[] args) {
        //JSON parser object to parse read file
        JSONParser jsonParser = new JSONParser();

        try (FileReader reader =
                     new FileReader(
                             "/Users/austinbrummett/IdeaProjects/mintelInterviewInJava/src/main/java/data.json"
                     ))
        {
            //Read JSON file
            Object obj = jsonParser.parse(reader);

            JSONArray dataList = (JSONArray) obj;

            calculateUniqueStatuses(dataList); // question 1
            System.out.println("---------------------------");
            getTopFiveUsers(dataList); // question 2
            System.out.println("---------------------------");
            howLongInStatusCode(dataList); // question 3
            System.out.println("---------------------------");
            errorThreeTwice(dataList); // question 4
            System.out.println("---------------------------");
            mostCommonPath(dataList); // question 5

        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        } catch (ParseException e) {
            e.printStackTrace();
        }
    }



    // Question 1: Return the number of unique statuses
    private static void calculateUniqueStatuses(JSONArray dataList) {
        LinkedList<String> l = new LinkedList<>();
        int count = 0;
        for (Object o : dataList) {
            if(!l.contains(getStatus((JSONObject) o))){
                l.add(getStatus((JSONObject) o));
            }
        }
        System.out.println(l.size());
    }
    // Question 2: top 5 users based on number of processes
    private static void getTopFiveUsers(JSONArray data){
        Map<String, Integer> userProcessCount = new HashMap<>();
        for(var d : data){
            String userKey = getUser((JSONObject) d);
            if(!userProcessCount.containsKey(userKey)){
                userProcessCount.putIfAbsent(userKey, 1);
            }else{
                userProcessCount.compute(userKey, (key, val) -> val += 1);
            }
        }
        Map<String, Integer> sortedMap = userProcessCount.
                entrySet().
                stream().
                sorted(Map.Entry.<String, Integer>comparingByValue().reversed())
                .collect(Collectors.toMap(
                        Map.Entry::getKey,
                        Map.Entry::getValue,
                        (oldVal, newVal) -> oldVal,LinkedHashMap::new
                ));
        int i = 0;
        Iterator<Map.Entry<String, Integer>> itr = sortedMap.entrySet().iterator();
        while(itr.hasNext()){
            Map.Entry<String, Integer> entry = itr.next();
            if(i == 5) break;
            System.out.println(entry.getKey() + ": " + entry.getValue());
            i++;
        }

    }
    // Question 3: How long in each status code with error
    private static void howLongInStatusCode(JSONArray data){
        int size = 1;
        int sum = 0;

        for(Object d : data){
            String statusCode =getStatus((JSONObject) d);
            if(statusCode.equals("8951")){
                String endTime = getEndTime((JSONObject) d);
                if(!endTime.equals("")){
                    String startTime = getStartTime((JSONObject) d);
                    sum += (Integer.valueOf(endTime) - Integer.valueOf(startTime));
                    size += 1;
                }
            }
        }

        System.out.println(sum/size);
    }
    // Question 4: error code ending in 3 twice
    private static void errorThreeTwice(JSONArray dataList) {
        // How often does a piece_id have an error code
        // ending in 3 more than once
        int count = 0;

        Map<String, Integer> errorStatusCount = new HashMap<>();
        for(var d : dataList){
            String pieceKey = getPieceID((JSONObject) d);
            if(!errorStatusCount.containsKey(pieceKey)){
                errorStatusCount.putIfAbsent(pieceKey, 0);
            }else{
                if(getStatus((JSONObject) d).endsWith("3"))
                    errorStatusCount.compute(pieceKey, (key, val) -> val += 1);
            }
        }

        for(var v : errorStatusCount.values()){
            if(v >= 2)
                count++;
        }
        System.out.println(100.00*(count/dataList.size())+" %");
    }
    // Question 5: most common path traveled
    private static void mostCommonPath(JSONArray dataList) {
        Map<String, ArrayList<Pair<String, Integer>>> paths = new HashMap<>();

        // create all the paths as pairs in format
        // piece_id: (status, start_time)
        for(Object d : dataList){
            String key = getPieceID((JSONObject) d);
            String status = getStatus((JSONObject) d);
            Integer startTime = Integer.valueOf(getStartTime((JSONObject) d));
            if(paths.containsKey(key)){
                paths.get(key).add(new Pair(status, startTime));
            }else{
                Pair<String, Integer> pair = new Pair<>(status, startTime);
                ArrayList<Pair<String,Integer>> p = new ArrayList<Pair<String, Integer>>();
                p.add(pair);
                paths.putIfAbsent(key, p);
            }
        }
        // Sorted based on time since epoch
        ArrayList<ArrayList<Pair<String, Integer>>> p = new ArrayList<>();
        for(var k : paths.values()){
            Collections.sort(k);
            p.add(k);
        }

       // paths turned into strings
       ArrayList<String> allPathsAsStrings = unpackPairedData(p);
       Map<String, Integer> pathCountMap = new HashMap<>();
       for(var item : allPathsAsStrings){
           if(pathCountMap.containsKey(item)){
               pathCountMap.computeIfPresent(item, (key, val) -> val += 1);
           }else{
               pathCountMap.putIfAbsent(item, 1);
           }
       }

       // convert to sorted set
        Map<String, Integer> sortedMap = pathCountMap.
                entrySet().
                stream().
                sorted(Map.Entry.<String, Integer>comparingByValue().reversed())
                .collect(Collectors.toMap(
                        Map.Entry::getKey,
                        Map.Entry::getValue,
                        (oldVal, newVal) -> oldVal,LinkedHashMap::new
                ));
        System.out.println(sortedMap.entrySet().iterator().next().getKey()
                + "--> " + sortedMap.entrySet().iterator().next().getValue());

    }

    private static ArrayList<String> unpackPairedData(ArrayList<ArrayList<Pair<String, Integer>>> data){
        // ArrayList<ArrayList<String>> unpackedData = new ArrayList<>();
        ArrayList<String> stringData = new ArrayList<>();
        for(var line : data){
            // ArrayList<String> items = new ArrayList<>();
            String temp = "";
            for(var item : line){
                if(temp.contains(item.getValue0()))
                    continue;
                else
                    temp += item.getValue0() + " ";

                // items.add(item.getValue0());
            }
            stringData.add(temp);
            //unpackedData.add(items);
        }

        return stringData;
    }
    // Getters
    // gets the status
    private static String getStatus(JSONObject data){
        return data.get("status").toString();
    }

    private static String getUser(JSONObject data){
        return (data.get("user_id") == null) ? "null" : data.get("user_id").toString();
    }

    private static String getEndTime(JSONObject data){
        return(data.get("end_time") == null) ? "" : data.get("end_time").toString();
    }

    private static String getPieceID(JSONObject data){
        return data.get("piece_id").toString();
    }

    private static String getStartTime(JSONObject data) {
        return data.get("start_time").toString();
    }


    // Proof of parsing ability
    private static void parseData(JSONObject data)
    {

        //Get piece_id
        String pieceId = getPieceID(data);
        System.out.println("Piece Id: " + pieceId);
        //Get start_time
        String startTime = getStartTime(data);
        System.out.println("Start Time: " + startTime);

        //Get userId
        String userId = getUser(data);
        System.out.println("User ID: " + userId);

        //Get endTime

        String endTime = getEndTime(data);
        System.out.println("End Time: " + endTime);

        // Get Unique ID
        String historyID = (String) data.get("id").toString();
        System.out.println("ID: " + historyID);

        String status = getStatus(data);
        System.out.println("Status: " + status);
    }
}