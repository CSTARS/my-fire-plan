<?php
    include_once "FusionTableWrapper.php";

    const API_KEY = "YOUR_API_KEY_HERE";
    const TABLE_ID = "YOUR_ENCRYPTED_TABLE_ID_HERE";
    const CLIENT_EMAIL = "YOUR_OAUTH_CLIENT_EMAIL_HERE";
    const PRIVATE_KEY = "PHYSICAL_PATH_TO_YOUR_PRIVATE_KEY_FILE_HERE";

    $wrapper = new FusionTableWrapper();
    $wrapper->InitializeWithCreditials(CLIENT_EMAIL, PRIVATE_KEY, TRUE);
    //$wrapper->InitializeWithCreditials(CLIENT_EMAIL, PRIVATE_KEY);
    //$wrapper->InitializeWithApiKey(API_KEY);


    $arr = $wrapper->ExecuteQuery("SELECT * FROM " . TABLE_ID);
    print_r($arr);

    $arr = $wrapper->ExecuteQuery("INSERT INTO " .TABLE_ID .
            " (Text, Number, Location, Date) VALUES ('Hello from PHP', 24, 'Sacramento, CA', '10/12/12')");
    print_r($arr);

    $arr = $wrapper->ExecuteQuery("SELECT ROWID FROM ".TABLE_ID.
            " WHERE Number = 24");
    print_r($arr);
    if ($arr && count($arr) > 0)
    {
        $query = "UPDATE ".TABLE_ID." SET Text = 'PHP Rocks' WHERE ROWID='".
                $arr[count($arr)-1]["rowid"]."'";

        $result = $wrapper->ExecuteQuery($query);
        print_r($result);


        $query = "DELETE FROM ".TABLE_ID." WHERE ROWID = '".
                $arr[count($arr)-1]["rowid"]."'";
        $result = $wrapper->ExecuteQuery($query);
        print_r($result);
    }
?>
