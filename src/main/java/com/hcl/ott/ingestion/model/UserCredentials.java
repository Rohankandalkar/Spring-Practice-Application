package com.hcl.ott.ingestion.model;

public class UserCredentials
{
    private String host;
    private String user;
    private String password;
    private String port;
    private String remoteFile;

    public UserCredentials()
    {
        // TODO Auto-generated constructor stub
    }

    public UserCredentials(String host, String user, String password, String port, String remoteFile)
    {
        super();
        this.host = host;
        this.user = user;
        this.password = password;
        this.port = port;
        this.remoteFile = remoteFile;
    }

    public String getHost()
    {
        return host;
    }

    public void setHost(String host)
    {
        this.host = host;
    }

    public String getUser()
    {
        return user;
    }

    public void setUser(String user)
    {
        this.user = user;
    }

    public String getPassword()
    {
        return password;
    }

    public void setPassword(String password)
    {
        this.password = password;
    }

    public String getPort()
    {
        return port;
    }

    public void setPort(String port)
    {
        this.port = port;
    }

    public String getRemoteFile()
    {
        return remoteFile;
    }

    public void setRemoteFile(String remoteFile)
    {
        this.remoteFile = remoteFile;
    }
    
    

}
