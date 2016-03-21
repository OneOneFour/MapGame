<?php
//array for json response
$response = array();

    
//call db_connect class
    require'db_connect.php';

//DML Query, IF statement runs if delete successful
    if($update = $conn->query("DELETE FROM locations")){
        //success
        $response["success"]=1;
        $response["message"]="locations deleted";
        
        //echo JSON
        echo json_encode($response);
    } else {
        //no contact found 
        $response["success"]=0;
        $response["message"]="No locations found";
        //echo JSON 
        echo json_encode($response);
    }
?>