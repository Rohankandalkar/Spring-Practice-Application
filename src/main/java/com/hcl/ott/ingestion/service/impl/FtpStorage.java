package com.hcl.ott.ingestion.service.impl;

import java.io.IOException;
import java.io.InputStream;
import java.net.SocketException;
import java.util.HashMap;
import java.util.Map;

import org.apache.commons.net.ftp.FTP;
import org.apache.commons.net.ftp.FTPClient;
import org.apache.commons.net.ftp.FTPFile;
import org.apache.commons.net.ftp.FTPReply;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import com.hcl.ott.ingestion.exception.IngestionException;
import com.hcl.ott.ingestion.model.UserCredentials;
import com.hcl.ott.ingestion.util.IngestionConstants;

/**
 * FTP Storage component
 * 
 * @author kandalakar.r
 *
 */
@Component
public class FtpStorage
{

    private static final Logger logger = LoggerFactory.getLogger(FtpStorage.class);


    /**
     * Download requested files InputStram from FTP Server using FTP Client
     * 
     * @param UserCredentials - Credential to connect FTP Server and download Requested file
     * @return InputStream - InputStream of requested file
     * 
     * @throws SocketException
     * @throws IOException
     * @throws IngestionException 
     */
    public Map<String, Object> downloadFile(UserCredentials userCredentials) throws SocketException, IOException, IngestionException
    {

        logger.debug(" FTP SERVICE : GET FTP CLIENT TO DOWNLOAD FILE FROM FTP SERVER ");

        FTPClient ftpClient = getFTPClient(userCredentials);

        FTPFile file = ftpClient.mlistFile(userCredentials.getRemoteFile());
        long size = file.getSize();

        InputStream mediaFileStream = ftpClient.retrieveFileStream(userCredentials.getRemoteFile());

        boolean success = ftpClient.completePendingCommand();
        if (success)
        {
            logger.info(" ALL FTP REQUESTED COMMANDS ARE COMPLETED ");
            if (ftpClient.isConnected())
            {
                logger.info(" DISCONNECT FTP CLIENT FROM SERVER");
                ftpClient.logout();
                ftpClient.disconnect();
            }
        }
        else
        {
            System.out.println(" commands " + success);
        }
        logger.debug(" FTP SERVICE : SUCCESSFULLY DOWNLOADED FILE FROM FTP SERVER ");

        Map<String, Object> map = new HashMap<>();
        map.put(IngestionConstants.FTP_CONTENTLENGTH, size);
        map.put(IngestionConstants.FTP_INPUTSTREAM, mediaFileStream);
        return map;
    }


    /**
     * Returns Configured FTP Client to retrieve file from FTP server
     * 
     * @param UserCredentials - Credential to connect FTP Server and download Requested file
     * @return FTPClient
     * @throws SocketException - Thrown to indicate that there is an error creating or accessing a Socket.
     * @throws IOException - Signals that an I/O exception of some sort has occurred.
     * @throws IngestionException 
     */
    private FTPClient getFTPClient(UserCredentials userCredentials) throws SocketException, IOException, IngestionException
    {
        logger.debug(" MAKING FTP Client FOR FTP SERVER OPERTION ");
        int port = Integer.parseInt(userCredentials.getPort());
        FTPClient ftpClient = new FTPClient();//FTP CLIENT TO CONNECT FTP SERVER
        ftpClient.connect(userCredentials.getHost(), port);

        int replyCode = ftpClient.getReplyCode();
        if (!FTPReply.isPositiveCompletion(replyCode))
        {
            logger.error("Operation failed. Server reply code: " + replyCode);
            throw new IngestionException(" Operation failed. Server reply code: \" + replyCode ");
        }
        boolean success = ftpClient.login(userCredentials.getUser(), userCredentials.getPassword());
        if (success)
            logger.info(" FTP CLIENT : SUCCESSFULLY LOGIN TO FTP SERVER WITH USERNAME : " + userCredentials.getUser());
        ftpClient.setFileType(FTP.BINARY_FILE_TYPE);

        return ftpClient;
    }

}
