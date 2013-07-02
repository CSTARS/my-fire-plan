<?php
	define("RESPONSE_KIND", "fusiontables#sqlresponse");

	Class FusionTableJSONParser
	{
		public static function GetData($results)
		{

			$retArray = array();
			$jsonObj = json_decode($results);

			if (isset($jsonObj->kind) && 
				 $jsonObj->kind == RESPONSE_KIND &&
				 isset($jsonObj->columns) && 
				 isset($jsonObj->rows))
			{
				foreach ($jsonObj->rows as $row)
				{
					$colIndx = 0;
					$rowArray = array();
					foreach ($row as $col)
					{
						$colName = $jsonObj->columns[$colIndx++];
						$rowArray[$colName] = $col;
					}
					$retArray[] = $rowArray;
				}

			}
			else
			{
				throw new Exception("Unknown JSON object");
			}
			return $retArray;
		}

	}
?>
