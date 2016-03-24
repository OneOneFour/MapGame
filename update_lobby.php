<?php
//array for json response
$response = array();

//check for required fields
if(isset($_GET['lobbyID'])){
    //trim fields from HTTP GET, store in variables 
    $ID= trim($_GET['lobbyID']);
    
    //call db_connect class
    require 'db_connect.php';
    
//IF statement for DML query, runs if successful 
    if($update = $conn->query("UPDATE lobby SET gameBegun = 1 WHERE lobbyID = 'lobbyID'")){
        //successfully updated 
        $response["success"]=1;
        $response["message"]="gameBegun updated";
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