<?php
    // Check for the required curl extensions, the wrapper api
    // won't function without them.
    if (! function_exists('curl_init')) {
          throw new Exception('FusitonTbleWrapper requires the CURL PHP extension');
    }
   if (!function_exists('openssl_x509_read')) {
              throw new Exception(
                          'FusionTableWrapper needs the openssl PHP extension');
    }

     if (! function_exists('json_decode')) {
           throw new Exception('Google PHP API Client requires the JSON PHP extension');
     }

      if (! function_exists('http_build_query')) {
            throw new Exception('Google PHP API Client requires http_build_query()');
      }

        if (! ini_get('date.timezone') && function_exists('date_default_timezone_set')) {
              date_default_timezone_set('UTC');
        }



    define("FUSIONTABLE_ENDPOINT", "https://www.googleapis.com/fusiontables/v1/query");
    define("GOOGLE_OAUTH2_ENDPOINT", "https://accounts.google.com/o/oauth2/token");

    Class FusionWebRequest
    {
        public function __construct()
        {
        }

        public static function GETRequest($query, $authToken, $apiKey)
        {
            $ret = "";
            $formData = array(
                "sql" => $query,
            );
            if ($apiKey != "")
            {
                $formData["key"] = $apiKey;
            }
            $paramStr = self::formURLParam($formData);
            $req = FUSIONTABLE_ENDPOINT . $paramStr;
            $client = curl_init();

            if ($authToken != "" && $authToken != NULL)
            {
                curl_setopt($client, CURLOPT_HTTPHEADER,
                        array("Authorization: Bearer ".$authToken));
            }

            curl_setopt($client, CURLOPT_RETURNTRANSFER,true);
            curl_setopt($client, CURLOPT_FOLLOWLOCATION, 0);
            curl_setopt($client, CURLOPT_FAILONERROR, true);
            curl_setopt($client, CURLOPT_SSL_VERIFYPEER, false);
            curl_setopt($client,    CURLOPT_HEADER, false);
            curl_setopt($client,    CURLOPT_VERBOSE, true);

            curl_setopt($client, CURLOPT_URL, $req);

            $result = curl_exec($client);
            $curlerror = curl_error($client);
            $statuscode = curl_getinfo($client, CURLINFO_HTTP_CODE);
            curl_close($client);

            if ($statuscode == 200)
            {
                $ret = $result;
            }
            else
            {
                self::handleWebException($statuscode, $curlerror);
            }
            return $ret;
        }

        public static function POSTRequest($query, $authToken)
        {
            $ret = "";
            $formData = array(
                "sql" => $query,
            );
            $paramStr = self::formURLParam($formData);
            $req = FUSIONTABLE_ENDPOINT . $paramStr;
            $client = curl_init();

            if ($authToken != "")
            {
                curl_setopt($client, CURLOPT_HTTPHEADER,
                    array("Authorization: Bearer ".$authToken));
            }
            curl_setopt($client, CURLOPT_RETURNTRANSFER,true);
            curl_setopt($client, CURLOPT_FOLLOWLOCATION, 0);
            curl_setopt($client, CURLOPT_FAILONERROR, false);
            curl_setopt($client, CURLOPT_SSL_VERIFYPEER, false);
            curl_setopt($client,    CURLOPT_HEADER, false);
            curl_setopt($client,    CURLOPT_VERBOSE, false);

            curl_setopt($client, CURLOPT_URL, $req);
            curl_setopt($client, CURLOPT_POST, true);
            curl_setopt($client, CURLOPT_POSTFIELDS, $paramStr);
            curl_setopt($client, CURLOPT_CONNECTTIMEOUT, 2);
            curl_setopt($client, CURLOPT_RETURNTRANSFER, 1);
            curl_setopt($client, CURLOPT_SSL_VERIFYPEER, 0);

            $result = curl_exec($client);
            $curlerror = curl_error($client);
            $statuscode = curl_getinfo($client, CURLINFO_HTTP_CODE);
            curl_close($client);

            if ($statuscode == 200)
            {
                $ret = $result;
            }

            else
            {
                self::handleWebException($statuscode, $curlerror);
            }
            return $ret;
        }

        private static function handleWebException($status, $curlerror)
        {
            if ($status == 302) // Moved Temporarily
            {
                throw new Exception($curlerror . "Google redirected your request - Please check that you are authenticated, or that you are querying a public table.");
            }
            else if ($status == 400) // Bad Request
            {
                throw new Exception($curlerror . "Google says that your request was not valid - Please check your query.");
            }
            else
            {
                throw new Exception($curlerror . "Google responded with an error. (" .
                                $status. ")"  );
            }
        }

        private static function formURLParam($array)
        {
            $output = http_build_query($array);
            return "?" . $output;
        }
    }

    Class GoogleOAuth2ServiceAccount
    {
        const tokenValidFor = 3600;

        public static function getAccessToken($email, $certificatePath, $isReadWrite = FALSE)
        {
            $token = "";
            $client = curl_init();
            $formData = array(
                "grant_type" => "assertion",
                "assertion_type" => "http://oauth.net/grant_type/jwt/1.0/bearer",
                "assertion" => self::getAssertion($email, $certificatePath, $isReadWrite),
            );
            $formStr = http_build_query($formData, '', '&');
            $httpHeader = array("content-type: application/x-www-form-urlencoded",
                            "content-length: ". strlen($formStr));

            curl_setopt($client, CURLOPT_RETURNTRANSFER,true);
            curl_setopt($client, CURLOPT_FOLLOWLOCATION, 0);
            curl_setopt($client, CURLOPT_FAILONERROR, false);
            curl_setopt($client, CURLOPT_SSL_VERIFYPEER, false);
            curl_setopt($client,    CURLOPT_HEADER, false);
            curl_setopt($client,    CURLOPT_VERBOSE, false);

            curl_setopt($client, CURLOPT_URL, GOOGLE_OAUTH2_ENDPOINT);
            curl_setopt($client, CURLOPT_HTTPHEADER, $httpHeader);
            curl_setopt($client, CURLOPT_POSTFIELDS, $formStr);
            curl_setopt($client, CURLOPT_CUSTOMREQUEST, "POST");

            $result = curl_exec($client);
            $curlerror = curl_error($client);
            $statuscode = curl_getinfo($client, CURLINFO_HTTP_CODE);
            curl_close($client);

            if ($statuscode == 200)
            {
                $obj = json_decode($result);
                if (array_key_exists("access_token", $obj))
                {
                    $token = $obj->{"access_token"};
                }
                else
                {
                    throw new Exception("Access Token does not exists");
                }
            }
            else
            {
                throw new Exception($curlerror . "Google responded with an error." . $statuscode);
            }
            return $token;
        }

        private static function getAssertion($email, $path, $isReadWrite)
        {
            $claimset = self::base64Encoding(self::getClaimset($email, $isReadWrite));
            $header = self::base64Encoding(self::getHeader("RS256", "JTW"));

            $payload = $header . "." . $claimset;
            $signature = self::rs256Signature($payload, $path);

            $assertion = $header . "." .
                             $claimset . "." .
                             self::base64Encoding($signature);

            return $assertion;
        }

        private static function rs256Signature($message, $certPath)
        {
            $password = "notasecret";
            $p12 = file_get_contents($certPath);
            $certs = array();
            $signautre = "";

            if (!openssl_pkcs12_read($p12, $certs, $password))
            {
                throw new Exception("Unable to parse the p12 file.  " .
                "Is this a .p12 file?  Is the password correct?  OpenSSL error: " .
                openssl_error_string());
            }

            if (!array_key_exists("pkey", $certs) || !$certs["pkey"])
            {
                throw new Exception("No private key found in p12 file.");
            }
            $privateKey = openssl_pkey_get_private($certs["pkey"]);
            if (!$privateKey)
            {
                throw new Exception("Unable to load private key in ");
            }
            if (!openssl_sign($message, $signature, $privateKey, "sha256"))
            {
                throw new Exception("Unable to sign data");
            }

            return $signature;
        }

        private static function getClaimSet($email, $isReadWrite)
        {
            $scope = "https://www.googleapis.com/auth/fusiontables" .
                ($isReadWrite ? "" : ".readonly");
            $aud = "https://accounts.google.com/o/oauth2/token";
            $iat = time();
            $exp = $iat + self::tokenValidFor;

            $jwtParams = array(
                "iss" => $email,
                "scope" => $scope,
                "aud" => $aud,
                "exp" => $exp,
                "iat" => $iat,
            );

            $jwt = self::jsonSerialize($jwtParams);
            return $jwt;
        }

        private static function getHeader($algorithm, $type)
        {
            $headerParams = array(
                "alg" => $algorithm,
                "typ" => $type,
                );

            $header = self::jsonSerialize($headerParams);
            return $header;
        }

        private static function base64Encoding($input)
        {
            $output = base64_encode($input);
            $output = str_replace(array('+', '/', '\r', '\n', '='),
                array('-', '_'),
                $output);
            return $output;
        }

        private static function jsonSerialize($array)
        {
            return json_encode($array);
        }
    }
?>
