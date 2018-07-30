package com.hcl.ott.ingestion.service.impl;

import java.io.IOException;
import java.io.InputStream;
import java.net.SocketException;
import java.util.HashMap;
import java.util.Map;

import org.apache.commons.net.ftp.FTP;
import org.apache.commons.net.ftp.FTPClient;
import org.apache.commons.net.ftp.FTPFile;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

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
     * @param hostName
     * @param userName
     * @param password
     * @param portNumber
     * @param remoteFile
     * @return InputStream - InputStream of requested file
     * 
     * @throws SocketException
     * @throws IOException
     */
    public InputStream downloadFile(String hostName, String userName, String password, String portNumber, String remoteFile) throws SocketException, IOException
    {
        int port = Integer.parseInt(portNumber);

        logger.debug(" GET FTP CLIENT TO DOWNLOAD FILE FROM FTP SERVER ");
        FTPClient ftpClient = getFTPClient(hostName, userName, password, port);
        InputStream mediaFileStream = ftpClient.retrieveFileStream(remoteFile);
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
        return mediaFileStream;
    }


    /**
     * Returns Configured FTP Client to retrive file from ftp server
     * 
     * @param hostName
     * @param userName
     * @param password
     * @param port
     * @return FTPClient
     * @throws SocketException - Thrown to indicate that there is an error creating or accessing a Socket.
     * @throws IOException - Signals that an I/O exception of some sort has occurred.
     */
    private FTPClient getFTPClient(String hostName, String userName, String password, int port) throws SocketException, IOException
    {
        logger.debug(" MAKING FTP Client FOR FTP SERVER OPERTION ");
        FTPClient ftpClient = new FTPClient();//FTP CLIENT TO CONNECT FTP SERVER
        ftpClient.connect(hostName, port);
        ftpClient.login(userName, password);
        ftpClient.enterLocalPassiveMode();
        ftpClient.setFileType(FTP.BINARY_FILE_TYPE);

        return ftpClient;
    }


    public Map<String, InputStream> downloadFTPFiles(String hostName, String userName, String password, int port) throws SocketException, IOException
    {
        FTPClient ftpClient = getFTPClient(hostName, userName, password, port);
        String Directory = "/HCL_ECMS/Video/";

        FTPFile[] files = ftpClient.listFiles("/HCL_ECMS/Video/");
        Map<String, InputStream> fileMap = new HashMap<String, InputStream>();

        for (FTPFile ftpFile : files)
        {
            if (ftpFile.isFile())
            {
                InputStream tempStream = ftpClient.retrieveFileStream(Directory + ftpFile.getName());
                boolean success = ftpClient.completePendingCommand();
                if (success)
                {
                    fileMap.put(ftpFile.getName(), tempStream);
                }
            }
        }

        return fileMap;

    }

}
