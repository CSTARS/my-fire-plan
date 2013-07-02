<?php
    include_once "FusionWebRequest.php";
    include_once "FusionTableJSONParser.php";

    define("NUMBER_OF_TRIES", 3);
    define("THROTTLE_WAIT_TIME", 1);

    Class FusionTableWrapper
    {
        private $clientID;
        private $privateKeyPath;

        public $apiKey;
        public $authToken;

        public function InitializeWithCreditials($emailAddress, $keyFile, $isReadWrite = false)
        {
            $this->clientID = $emailAddress;
            $this->privateKeyPath = $keyFile;

            $this->authToken = GoogleOAuth2ServiceAccount::getAccessToken($emailAddress, $keyFile, $isReadWrite);
        }

        public function InitializeWithApiKey($apiKey)
        {
            $this->apiKey = $apiKey;
        }

        public function ExecuteQuery($query)
        {
            $retries = 0;
            $retry = FALSE;
            $rows = array();
            $queryLower = ltrim(strtolower($query));
            if (preg_match("/^select|^show|^describe/i", $query))
            {
                do{
                    if($retry) sleep(THROTTLE_WAIT_TIME);
                    $results = FusionWebRequest::GETRequest($query,
                                                                    $this->authToken,
                                                                    $this->apiKey,
                                                                    $retry);
                }while(!$results && $retry && ++$retries < NUMBER_OF_TRIES);
                if ($results)
                {
                    $rows = FusionTableJSONParser::GetData($results);
                }
            }
            elseif (preg_match("/^create|^insert|^update|^delete/i", $query))
            {
                do{
                    if($retry) sleep(THROTTLE_WAIT_TIME);
                    $results = FusionWebRequest::POSTRequest($query,
                                                                    $this->authToken,
                                                                    $retry);

                }while(!$results && $retry && ++$retries < NUMBER_OF_TRIES);

                if ($results)
                {
                    $data = FusionTableJSONParser::GetData($results);
                    if(count($data) > 0)
                    {
                        $rows["Results"] = TRUE;
                    }
                    else
                    {
                        $rows["Results"] = FALSE;
                    }
                }
            }
            else
            {
                throw new Exception("Unsupported SQL command");
            }
            return $rows;
        }
    }
?>
