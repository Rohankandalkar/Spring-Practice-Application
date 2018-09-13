package com.hcl.ott.ingestion.service.impl;

import java.io.IOException;
import java.io.InputStream;
import java.net.SocketException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.net.ftp.FTP;
import org.apache.commons.net.ftp.FTPClient;
import org.apache.commons.net.ftp.FTPFile;
import org.apache.commons.net.ftp.FTPReply;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import com.hcl.ott.ingestion.data.UserCredentials;
import com.hcl.ott.ingestion.exception.IngestionException;
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
        logger.info(" FTP SERVICE : DOWNLOAD REQUESTED FILES FROM FTP SERVER  ");

        logger.debug(" FTP SERVICE : GET FTP CLIENT TO DOWNLOAD FILE FROM FTP SERVER ");
        FTPClient ftpClient = connectFTPServer(userCredentials);

        FTPFile file = ftpClient.mlistFile(userCredentials.getRemoteFile().get(0));
        long fileSize = file.getSize();

        //downloading requested file in the form of "InputStream"
        InputStream mediaFileStream = ftpClient.retrieveFileStream(userCredentials.getRemoteFile().get(0));

        //waiting to complete commands
        boolean success = ftpClient.completePendingCommand();
        if (success)
        {
            logger.info(" FTP REQUESTED COMMAND IS COMPLETED ");
            disconnectFtp(ftpClient);
        }
        else
        {
            logger.debug(" FTP REQUESTED COMMAND IS NOT COMPLETED SOME PROBLEM OCCERS DURING DOWNLOADING ");
            disconnectFtp(ftpClient);
        }

        Map<String, Object> map = new HashMap<>();
        map.put(IngestionConstants.FTP_CONTENTLENGTH, fileSize);
        map.put(IngestionConstants.FTP_INPUTSTREAM, mediaFileStream);

        logger.info(" FTP SERVICE : SUCCESSFULLY DOWNLOADE FILE NAME : " + userCredentials.getRemoteFile().get(0) + " SIZE : " + fileSize);

        return map;
    }


    /**
     * Download requested list of files from FTP Server using FTP Client
     * 
     * @param UserCredentials - Credential to connect FTP Server and download Requested files 
     * @return Map - Map of InputStream's and size of requested file
     * 
     * @throws SocketException
     * @throws IOException
     * @throws IngestionException 
     */
    public Map<String, Object> downloadMultipalFiles(UserCredentials userCredentials) throws SocketException, IOException, IngestionException
    {
        logger.info(" FTP SERVICE : DOWNLOAD REQUESTED LIST OF FILES FROM FTP SERVER  ");

        logger.debug(" FTP SERVICE : GET FTP CLIENT TO DOWNLOAD FILE FROM FTP SERVER ");
        FTPClient ftpClient = connectFTPServer(userCredentials);

        Map<String, Object> inputStreamMap = new HashMap<>();
        Map<String, Object> sizeMap = new HashMap<>();

        List<String> remoteFileList = userCredentials.getRemoteFile();

        for (String remoteFileName : remoteFileList)
        {
            FTPFile file = ftpClient.mlistFile(remoteFileName);
            long fileSize = file.getSize();

            //SAVE FILE'S SIZE IN MAP WITH ITS NAME AS KEY
            sizeMap.put(remoteFileName, fileSize);

            //DOWNLOAD REQUESTED FILE'S IN THE FORM OF INPUTSTREAM
            InputStream mediaFileStream = ftpClient.retrieveFileStream(remoteFileName);

            //INSURES PENDING COMMANDS ARE COMPLETED
            ftpClient.completePendingCommand();

            //SAVE FILE'S INPUTSTREAM IN MAP WITH ITS NAME AS KEY
            inputStreamMap.put(remoteFileName, mediaFileStream);
        }

        logger.debug(" FTP SERVICE : ALL FTP REQUESTED COMMANDS ARE COMPLETED ");

        disconnectFtp(ftpClient);

        Map<String, Object> map = new HashMap<>();
        map.put(IngestionConstants.FTP_CONTENTLENGTH, sizeMap);
        map.put(IngestionConstants.FTP_INPUTSTREAM, inputStreamMap);

        logger.info(" FTP SERVICE : SUCCESSFULLY DOWNLOADED FILES FROM FTP SERVER ");

        return map;
    }


    /**
     * Returns Configured FTP Client to download file from FTP server
     * 
     * @param UserCredentials - Credential to connect FTP Server and download Requested file
     * @return FTPClient
     * @throws SocketException - Thrown to indicate that there is an error creating or accessing a Socket.
     * @throws IOException - Signals that an I/O exception of some sort has occurred.
     * @throws IngestionException 
     */
    private FTPClient connectFTPServer(UserCredentials userCredentials) throws SocketException, IOException, IngestionException
    {
        logger.debug("  FTP SERVICE :  MAKING FTP Client FOR FTP SERVER OPERTION ");

        int port = Integer.parseInt(userCredentials.getPort());

        //FTP CLIENT TO CONNECT FTP SERVER
        FTPClient ftpClient = new FTPClient();

        if (!ftpClient.isConnected())
        {
            ftpClient.connect(userCredentials.getHost(), port);

            int replyCode = ftpClient.getReplyCode();
            if (!FTPReply.isPositiveCompletion(replyCode))
            {
                logger.error("Operation failed. Server reply code: " + replyCode);
                throw new IngestionException(" Operation failed. Server reply code: \" + replyCode ");
            }

            boolean login = ftpClient.login(userCredentials.getUser(), userCredentials.getPassword());

            if (login)
                logger.info("  FTP SERVICE : FTP CLIENT : SUCCESSFULLY LOGIN TO FTP SERVER HOST " + userCredentials.getHost() + " WITH USERNAME : " + userCredentials.getUser());
            ftpClient.enterLocalPassiveMode();
            ftpClient.setFileType(FTP.BINARY_FILE_TYPE);
        }

        return ftpClient;
    }


    /**
     * Log out and disconnect from the server
     * @throws IOException 
     * @throws IngestionException
     */
    public void disconnectFtp(FTPClient ftpClient) throws IngestionException, IOException
    {
        if (ftpClient.isConnected())
        {
            logger.info("  FTP SERVICE :  DISCONNECT FTP CLIENT FROM SERVER");
            ftpClient.logout();
            ftpClient.disconnect();

        }
    }

}
