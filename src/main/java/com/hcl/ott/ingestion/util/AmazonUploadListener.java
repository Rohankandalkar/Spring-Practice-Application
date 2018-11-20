package com.hcl.ott.ingestion.util;

import java.text.SimpleDateFormat;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.amazonaws.event.ProgressEvent;
import com.amazonaws.event.ProgressEventType;
import com.amazonaws.event.ProgressListener;

public class AmazonUploadListener implements ProgressListener
{
    private int partNo;


    public AmazonUploadListener()
    {
       
    }


    public AmazonUploadListener(int partNo, long partLength)
    {
        super();
        this.partNo = partNo;
    }

    private static final Logger logger = LoggerFactory.getLogger(AmazonUploadListener.class);


    @Override
    public void progressChanged(ProgressEvent progressEvent)
    {
        ProgressEventType event = progressEvent.getEventType();

        SimpleDateFormat dateFormatter = new SimpleDateFormat("E, y-M-d 'at' h:m:s a z");

        switch (event)
        {

            case TRANSFER_STARTED_EVENT:

                logger.info(
                    " AMAZON EVENT LISTNER : START UPLOADIG FILE TO AWS START "
                        + dateFormatter.format(System.currentTimeMillis()));
                logger.info(" ");

                break;

            case TRANSFER_PART_STARTED_EVENT:

                this.partNo += 1;
                logger.info(" AMAZON EVENT LISTNER : UPLOADIG FILE TO AWS WITH PART START AT " + dateFormatter.format(System.currentTimeMillis()));
                logger.info(" AMAZON EVENT LISTNER : UPLOADIG FILE TO AWS WITH PART NUMBER " + this.partNo);
                logger.info(" ");

                break;

            case TRANSFER_PART_COMPLETED_EVENT:

                logger.info(" AMAZON EVENT LISTNER : UPLOADIG FILE TO AWS WITH PART COMPLETED " + dateFormatter.format(System.currentTimeMillis()));
                logger.info(" ");

                break;

            case TRANSFER_COMPLETED_EVENT:

                logger.info(" AMAZON EVENT LISTNER : END UPLOADIG FILE TO AWS COMPLETED " + dateFormatter.format(System.currentTimeMillis()));
                logger.info(" ");

                break;

            case TRANSFER_FAILED_EVENT:

                logger.info(" AMAZON EVENT LISTNER : UPLOADIG FILE TO AWS FAILED " + dateFormatter.format(System.currentTimeMillis()));

                break;

            default:
                break;
        }

    }

}
