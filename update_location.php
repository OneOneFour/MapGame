<?php
//array for json response
$response = array();

//check for required fields
if(isset($_GET['name'], $_GET['latitude'], $_GET['longitude'])){
    //trim fields from HTTP GET, store in variables 
    $name= trim($_GET['name']);
    $latitude= trim($_GET['latitude']);
    $longitude = trim($_GET['longitude']);
    
    //call db_connect class
    require 'db_connect.php';
    
//IF statement for DML query, runs if successful 
    if($update = $conn->query("UPDATE locations SET latitude = '$latitude', longitude='$longitude' WHERE userName='$name'")){
        //successfully updated 
        $response["success"]=1;
        $response["message"]="location updated";
        //echo JSON
        echo json_encode($response);
    } else {
        //failed to update row
        $response["success"]=0;
        $response["message"]="An error occured";
        //echo JSON 
        echo json_encode($response);
    }
} else {
    //required field is missing
    $response["success"]=0;
    $response["message"]="required field missing";
    
    //echo JSON 
    echo json_encode($response);
}
?>