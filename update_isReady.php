<?php
//array for json response
$response = array();

//check for required fields
if(isset($_GET['name'])){
    //trim fields from HTTP GET, store in variables 
    $name= trim($_GET['name']);
    
    //call db_connect class
    require 'db_connect.php';
    
//IF statement for DML query, runs if successful 
    if($update = $conn->query("UPDATE locations SET isReady = 1 WHERE userName='$name'")){
        //successfully updated 
        $response["success"]=1;
        $response["message"]="isReady updated";
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