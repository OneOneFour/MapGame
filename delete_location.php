<?php
//array for json response
$response = array();

//check for required fields
if(isset($_GET['name'])){
//trim id from HTTP GET and store in variable
    $name = $_GET['name'];
    
//call db_connect class
    require'db_connect.php';

//DML Query, IF statement runs if delete successful
    if($update = $conn->query("DELETE FROM locations WHERE userName = '$name'")){
        //success
        $response["success"]=1;
        $response["message"]="Location deleted";
        
        //echo JSON
        echo json_encode($response);
    } else {
        //no contact found 
        $response["success"]=0;
        $response["message"]="No location found";
        //echo JSON 
        echo json_encode($response);
    }
}else {
    //required field missing
    $response["success"]=0;
    $response["message"]="required field missing";
    //echo JSON
    echo json_encode($response);
}
?>