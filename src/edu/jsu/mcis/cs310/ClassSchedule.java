package edu.jsu.mcis.cs310;

import com.github.cliftonlabs.json_simple.*;
import com.opencsv.*;
import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.StringReader;
import java.io.StringWriter;
import java.util.List;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Iterator;

public class ClassSchedule {
    
    private final String CSV_FILENAME = "jsu_sp24_v1.csv";
    private final String JSON_FILENAME = "jsu_sp24_v1.json";
    
    private final String CRN_COL_HEADER = "crn";
    private final String SUBJECT_COL_HEADER = "subject";
    private final String NUM_COL_HEADER = "num";
    private final String DESCRIPTION_COL_HEADER = "description";
    private final String SECTION_COL_HEADER = "section";
    private final String TYPE_COL_HEADER = "type";
    private final String CREDITS_COL_HEADER = "credits";
    private final String START_COL_HEADER = "start";
    private final String END_COL_HEADER = "end";
    private final String DAYS_COL_HEADER = "days";
    private final String WHERE_COL_HEADER = "where";
    private final String SCHEDULE_COL_HEADER = "schedule";
    private final String INSTRUCTOR_COL_HEADER = "instructor";
    private final String SUBJECTID_COL_HEADER = "subjectid";
    
    public String convertCsvToJsonString(List<String[]> csv) {
      // Create iterator for the CSV data
    Iterator<String[]> iterator = csv.iterator();
    
    // Create JSON objects to structure the data
    JsonObject outerJsonObject = new JsonObject();
    JsonObject ScheduletypeObject = new JsonObject();
    JsonObject SubjectObject = new JsonObject();
    JsonObject CourseObject = new JsonObject();
    JsonArray SectionList = new JsonArray();

    // Get the headers from the first row of the CSV
    String[] headers = iterator.next();
    
    // Create a map to store the index of each header for quick access
    HashMap<String, Integer> headerList = new HashMap<>();
    for (int i = 0; i < headers.length; ++i) {
        headerList.put(headers[i], i);
    }
    
    String jsonString = "";

    // Process each row of the CSV
    while (iterator.hasNext()) {
        // Get the current row
        String[] currentRow = iterator.next();
        
        // Extract values from the current row
        Integer crn = Integer.valueOf(currentRow[headerList.get(CRN_COL_HEADER)]);
        Integer credit = Integer.valueOf(currentRow[headerList.get(CREDITS_COL_HEADER)]);
        String num = currentRow[headerList.get(NUM_COL_HEADER)];
        
        // Split 'num' into two parts
        Integer spaceIndex = num.indexOf(' ');
        String firstPart = num.substring(0, spaceIndex);
        String secondPart = num.substring(spaceIndex + 1);
        
        // Populate ScheduletypeObject and SubjectObject
        ScheduletypeObject.put(currentRow[headerList.get(TYPE_COL_HEADER)], currentRow[headerList.get(SCHEDULE_COL_HEADER)]);
        SubjectObject.put(firstPart, currentRow[headerList.get(SUBJECT_COL_HEADER)]);
        
        // Populate InnerCourseObject
        JsonObject InnerCourseObject = new JsonObject();
        InnerCourseObject.put(SUBJECTID_COL_HEADER, firstPart);
        InnerCourseObject.put(NUM_COL_HEADER, secondPart);
        InnerCourseObject.put(DESCRIPTION_COL_HEADER, currentRow[headerList.get(DESCRIPTION_COL_HEADER)]);
        InnerCourseObject.put(CREDITS_COL_HEADER, credit);
        CourseObject.put(currentRow[headerList.get(NUM_COL_HEADER)], InnerCourseObject);
        
        // Populate InnerSectionObject
        List<String> instructorList = Arrays.asList(currentRow[headerList.get(INSTRUCTOR_COL_HEADER)].split(", "));
        JsonObject InnerSectionObject = new JsonObject();
        InnerSectionObject.put(CRN_COL_HEADER, crn);
        InnerSectionObject.put(SUBJECTID_COL_HEADER, firstPart);
        InnerSectionObject.put(NUM_COL_HEADER, secondPart);
        InnerSectionObject.put(SECTION_COL_HEADER, currentRow[headerList.get(SECTION_COL_HEADER)]);
        InnerSectionObject.put(TYPE_COL_HEADER, currentRow[headerList.get(TYPE_COL_HEADER)]);
        InnerSectionObject.put(START_COL_HEADER, currentRow[headerList.get(START_COL_HEADER)]);
        InnerSectionObject.put(END_COL_HEADER, currentRow[headerList.get(END_COL_HEADER)]);
        InnerSectionObject.put(DAYS_COL_HEADER, currentRow[headerList.get(DAYS_COL_HEADER)]);
        InnerSectionObject.put(WHERE_COL_HEADER, currentRow[headerList.get(WHERE_COL_HEADER)]);
        InnerSectionObject.put(INSTRUCTOR_COL_HEADER, instructorList);

        // Add InnerSectionObject to SectionList
        SectionList.add(InnerSectionObject);

        // Update outerJsonObject with the latest data
        outerJsonObject.put("scheduletype", ScheduletypeObject);
        outerJsonObject.put("subject", SubjectObject);
        outerJsonObject.put("course", CourseObject);
        outerJsonObject.put("section", SectionList); 
    }
    
    // Serialize the outerJsonObject to JSON string
    jsonString = Jsoner.serialize(outerJsonObject);
    
    return jsonString ;  
        
        
    }
    
    public String convertJsonToCsvString(JsonObject json) {
        
     // Extract JSON objects from the provided JSON
    JsonArray sections = (JsonArray) json.get("section");
    JsonObject courses = (JsonObject) json.get("course");
    JsonObject subjects = (JsonObject) json.get("subject");
    JsonObject scheduletype = (JsonObject) json.get("scheduletype");

    String csvString="";

    try (StringWriter writer = new StringWriter();
         CSVWriter csvWriter = new CSVWriter(writer, '\t', '"', '\\', "\n")) {

        // Write CSV headers
        csvWriter.writeNext(new String[]{CRN_COL_HEADER, SUBJECT_COL_HEADER, NUM_COL_HEADER, DESCRIPTION_COL_HEADER, SECTION_COL_HEADER, TYPE_COL_HEADER,CREDITS_COL_HEADER,START_COL_HEADER,END_COL_HEADER,DAYS_COL_HEADER ,WHERE_COL_HEADER ,SCHEDULE_COL_HEADER ,INSTRUCTOR_COL_HEADER });// Write CSV headers"credits",

        // Iterate over sections and construct CSV rows
        for (Object sectionObj : sections) {
            JsonObject section = (JsonObject) sectionObj;

            // Extract relevant data from JSON objects
            String crn = String.valueOf(section.get(CRN_COL_HEADER));
            String subjectID = (String) section.get(SUBJECTID_COL_HEADER);
            String subject = (String) subjects.get(subjectID);
            String justnum = (String) section.get(NUM_COL_HEADER);
            String num = subjectID + " " + justnum;
            JsonObject innercourse = (JsonObject) courses.get(num);

            // Extract inner course details
            String description = (String) innercourse.get(DESCRIPTION_COL_HEADER);
            String sectionId = (String) section.get(SECTION_COL_HEADER);
            String type = (String) section.get(TYPE_COL_HEADER);
            String credits = String.valueOf(innercourse.get(CREDITS_COL_HEADER));
            String start = (String) section.get(START_COL_HEADER);
            String end = (String) section.get(END_COL_HEADER);
            String days = (String) section.get(DAYS_COL_HEADER);
            String where = (String) section.get(WHERE_COL_HEADER);
            String schedule = (String) scheduletype.get(type); 

            // Extract and format instructor details
            String instructor = String.join(", ", (List) section.get(INSTRUCTOR_COL_HEADER));

            // Write CSV row
            csvWriter.writeNext(new String[]{crn, subject, num, description, sectionId, type, credits, start, end, days, where, schedule, instructor});
        }

        // Convert the CSV writer content to string
        csvString = writer.toString();

    } catch (IOException e) {
        e.printStackTrace();
    }

    return csvString;
    }

        public JsonObject getJson() {

            JsonObject json = getJson(getInputFileData(JSON_FILENAME));
            return json;

        }

        public JsonObject getJson(String input) {

            JsonObject json = null;

            try {
                json = (JsonObject)Jsoner.deserialize(input);
            }
            catch (Exception e) { e.printStackTrace(); }

            return json;

        }

        public List<String[]> getCsv() {

            List<String[]> csv = getCsv(getInputFileData(CSV_FILENAME));
            return csv;

        }

        public List<String[]> getCsv(String input) {

            List<String[]> csv = null;

            try {

                CSVReader reader = new CSVReaderBuilder(new StringReader(input)).withCSVParser(new CSVParserBuilder().withSeparator('\t').build()).build();
                csv = reader.readAll();

            }
            catch (Exception e) { e.printStackTrace(); }

            return csv;

    }
    
    public String getCsvString(List<String[]> csv) {
        
        StringWriter writer = new StringWriter();
        CSVWriter csvWriter = new CSVWriter(writer, '\t', '"', '\\', "\n");
        
        csvWriter.writeAll(csv);
        
        return writer.toString();
        
    }
    
    private String getInputFileData(String filename) {
        
        StringBuilder buffer = new StringBuilder();
        String line;
        
        ClassLoader loader = ClassLoader.getSystemClassLoader();
        
        try {
        
            BufferedReader reader = new BufferedReader(new InputStreamReader(loader.getResourceAsStream("resources" + File.separator + filename)));

            while((line = reader.readLine()) != null) {
                buffer.append(line).append('\n');
            }
            
        }
        catch (Exception e) { e.printStackTrace(); }
        
        return buffer.toString();
        
    }
    
}