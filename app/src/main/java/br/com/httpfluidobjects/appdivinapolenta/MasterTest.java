package br.com.httpfluidobjects.appdivinapolenta;

/**
 * Created by gabrielweich on 25/09/17.
 */

import android.util.Log;

import java.net.*;

import net.wimpi.modbus.*;
import net.wimpi.modbus.msg.*;
import net.wimpi.modbus.io.*;
import net.wimpi.modbus.net.*;
import net.wimpi.modbus.procimg.SimpleRegister;

public class MasterTest{

    TCPMasterConnection con; //the connection
    /* Variables for storing the parameters */
    InetAddress addr; //the slave's address
    int port;


    public MasterTest(String ip, int port){
        con = null;
        try {
            this.addr = InetAddress.getByName(ip);
        } catch (UnknownHostException e) {
            e.printStackTrace();
        }
        this.port = port;
    }

     //Using jamod lib

    //Escreve um valor em um registrador
    public void writeRegisters(int register, int value){
        try {
            SimpleRegister reg = null;
            reg = new SimpleRegister(value);
            con = new TCPMasterConnection(addr);
            con.setPort(port);
            con.setTimeout(3000);
            con.connect();
            WriteSingleRegisterRequest write = new WriteSingleRegisterRequest(register, reg);
            ModbusTCPTransaction transaction = new ModbusTCPTransaction(con);
            transaction.setRequest(write);
            transaction.execute();
            con.close();
        }
        catch (Exception ex) {
            ex.printStackTrace();
            Log.d("TEST", "Erro no write reg");
            con.close();
        }

    }

    //Le o valor de um registrador
    public int readRegister(int register){
        int valor = 0;
        try {
            //2. Open the connection
            con = new TCPMasterConnection(addr);
            con.setPort(port);
            con.setTimeout(000);
            con.connect();
            ReadMultipleRegistersRequest request = new ReadMultipleRegistersRequest(register, 1);
            request.setUnitID(1);
            ReadMultipleRegistersResponse response = (ReadMultipleRegistersResponse) executeTransaction(con, request);
            valor = response.getRegisterValue(0);
            con.close();
            return valor;
        }catch (Exception ex) {
            ex.printStackTrace();
            con.close();
            return -1;
        }
    }

    //Executa a transacao e recebe a resposta
    private static ModbusResponse executeTransaction(TCPMasterConnection connection,
                                                     ModbusRequest request)
            throws ModbusIOException, ModbusSlaveException, ModbusException {
        ModbusTCPTransaction transaction = new ModbusTCPTransaction(connection);
        transaction.setRequest(request);
        transaction.execute();
        return transaction.getResponse();
    }


}
