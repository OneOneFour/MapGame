<?php
//array for json response
$response = array();

//check for required fields
if(isset($_GET['lobbyID'])){
//trim id from HTTP GET and store in variable
    $ID = $_GET['lobbyID'];
    
//call db_connect class
    require'db_connect.php';

//DML Query, IF statement runs if delete successful
    if($update = $conn->query("DELETE FROM lobby WHERE lobbyID = '$ID'")){
        //success
        $response["success"]=1;
        $response["message"]="Lobby deleted";
        
        //echo JSON
        echo json_encode($response);
    } else {
        //no contact found 
        $response["success"]=0;
        $response["message"]="No lobby found";
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