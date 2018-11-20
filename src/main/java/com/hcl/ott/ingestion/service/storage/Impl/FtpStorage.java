package com.hcl.ott.ingestion.service.storage.Impl;

import java.io.IOException;
import java.io.InputStream;
import java.net.SocketException;
import java.util.List;

import org.apache.commons.net.ftp.FTP;
import org.apache.commons.net.ftp.FTPClient;
import org.apache.commons.net.ftp.FTPFile;
import org.apache.commons.net.ftp.FTPReply;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import com.hcl.ott.ingestion.data.FileDetailsData;
import com.hcl.ott.ingestion.data.UserCredentials;
import com.hcl.ott.ingestion.exception.IngestionException;

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
     * This method download requested files InputStram from FTP Server using FTP Client
     * 
     * @param UserCredentials - Credential to connect FTP Server 
     * @param FileDetailsData - FileDetailsData with basic meta-data
     * @return FileDetailsData - InputStream and meta-data of requested file
     * 
     * @throws SocketException
     * @throws IOException
     * @throws IngestionException 
     */
    public FileDetailsData downloadFile(UserCredentials userCredentials, FileDetailsData fileDetails) throws SocketException, IOException, IngestionException
    {
        logger.debug(" FTP SERVICE : GET FTP CLIENT TO DOWNLOAD FILE FROM FTP SERVER ");
        FTPClient ftpClient = connectFTPServer(userCredentials);

        FTPFile file = ftpClient.mlistFile(fileDetails.getTitle());
        long fileSize = file.getSize();

        //downloading requested file in the form of "InputStream"
        InputStream mediaFileStream = ftpClient.retrieveFileStream(fileDetails.getTitle());

        //waiting to complete commands
        boolean success = ftpClient.completePendingCommand();
        if (success)
        {
            logger.debug(" FTP REQUESTED COMMAND IS COMPLETED ");
            disconnectFtp(ftpClient);
        }
        else
        {
            logger.debug(" FTP REQUESTED COMMAND IS NOT COMPLETED SOME PROBLEM OCCERS DURING DOWNLOADING ");
            disconnectFtp(ftpClient);
        }

        fileDetails.setFileSize(fileSize);
        fileDetails.setFileInputStream(mediaFileStream);

        logger.debug(" FTP SERVICE : SUCCESSFULLY DOWNLOADE FILE NAME : " + fileDetails.getTitle() + " SIZE : " + fileSize);

        return fileDetails;
    }


    /**
     * This method download requested list of files from FTP Server using FTP Client
     * 
     * @param UserCredentials - Credential to connect FTP Server and download Requested files 
     * @param List<FileDetailsData> - List of files InputStream and meta-data of files we have to download
     * @return List<FileDetailsData> - List of downloaded files InputStream and its meta-data 
     * 
     * @throws SocketException
     * @throws IOException
     * @throws IngestionException 
     */
    public List<FileDetailsData> downloadMultipalFiles(UserCredentials userCredentials, List<FileDetailsData> storageDetailsList)
        throws SocketException, IOException, IngestionException
    {
        logger.debug(" FTP SERVICE : DOWNLOAD REQUESTED LIST OF FILES FROM FTP SERVER  ");

        logger.debug(" FTP SERVICE : GET FTP CLIENT TO DOWNLOAD FILE FROM FTP SERVER ");
        FTPClient ftpClient = connectFTPServer(userCredentials);

        for (FileDetailsData remoteFile : storageDetailsList)
        {
            remoteFile.setFileSize(ftpClient.mlistFile(remoteFile.getTitle()).getSize());

            //DOWNLOAD REQUESTED FILE'S IN THE FORM OF INPUTSTREAM
            remoteFile.setFileInputStream(ftpClient.retrieveFileStream(remoteFile.getTitle()));

            //INSURES PENDING COMMANDS ARE COMPLETED
            ftpClient.completePendingCommand();

        }

        logger.debug(" FTP SERVICE : ALL FTP REQUESTED COMMANDS ARE COMPLETED ");

        disconnectFtp(ftpClient);

        logger.debug(" FTP SERVICE : SUCCESSFULLY DOWNLOADED FILES FROM FTP SERVER ");

        return storageDetailsList;
    }


    /**
     * This method returns Configured FTP Client to download file from FTP server
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
                logger.debug("  FTP SERVICE : FTP CLIENT : SUCCESSFULLY LOGIN TO FTP SERVER HOST " + userCredentials.getHost() + " WITH USERNAME : " + userCredentials.getUser());
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
            logger.debug("  FTP SERVICE :  DISCONNECT FTP CLIENT FROM SERVER ");
            ftpClient.logout();
            ftpClient.disconnect();

        }
    }

}
