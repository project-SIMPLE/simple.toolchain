using System.Net;
using System;
public static class StaticInformation
{
    public static string endOfGame { get; set; }
    private static string connectionId;

    public static string getId() {

        if (connectionId == null || connectionId.Length == 0)
        {
            string hostName = Dns.GetHostName(); // Retrive the Name of HOST
           try
            {

               IPAddress[] adresses = Dns.GetHostEntry(hostName).AddressList;
                string myIP = "127.0.0.1";
                foreach (IPAddress a in adresses)
                {
                    if (a.AddressFamily == System.Net.Sockets.AddressFamily.InterNetwork)
                    {
                        myIP = a.ToString();
                        break;
                    }
                }
                
                
                string lastIP = myIP.Contains(".") ? myIP.Split(".")[3] : "0";
                connectionId = "Player_" + lastIP;// + lastIP;
            } catch
            {
                connectionId = hostName;
            }
           
        }
        return connectionId;
    }
}
