<?php
//array for json response
$response = array();

//check for required fields
if(isset($_GET['name']),($_GET['newname'])){
    //trim fields from HTTP GET, store in variables 
    $name= trim($_GET['name']);
    $newname= trim($_GET['newname']);

    //call db_connect class
    require 'db_connect.php';
    
//IF statement for DML query, runs if successful 
    if($update = $conn->query("UPDATE lobby SET HostName= '$newname' WHERE HostName = '$name'")){
        //successfully updated 
        $response["success"]=1;
        $response["message"]="lobby owner updated";
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