import java.io.Serializable;
import java.security.Timestamp;
import java.util.Date;

import com.sankhya.util.SessionFile;
import com.sankhya.util.UIDGenerator;

import br.com.sankhya.extensions.actionbutton.AcaoRotinaJava;
import br.com.sankhya.extensions.actionbutton.ContextoAcao;
import br.com.sankhya.extensions.actionbutton.QueryExecutor;
import br.com.sankhya.jape.EntityFacade;
import br.com.sankhya.jape.core.JapeSession;
import br.com.sankhya.modelcore.util.EntityFacadeFactory;
import br.com.sankhya.ws.ServiceContext;

public class ArquivoRioCard implements AcaoRotinaJava {
    public void doAction(ContextoAcao contexto) throws Exception {
    	String referencia = contexto.getParam("REFERENCIA").toString();
        Date mDate = new Date(System.currentTimeMillis());
        String nomeArquivo = "RioCard" + (mDate.getMonth() + 1) + ".prn";
        
        QueryExecutor query = contexto.getQuery();
        query.setParam("REFERENCIA", contexto.getParam("REFERENCIA"));
        query.nativeSelect("SELECT LPAD(ROWNUM,5,0) AS NSR,"
        		+ "           LPAD(ROWNUM,2,0) AS TR,"
        		+ "           MATRICULA,"
        		+ "           LPAD(VALOR,9,'0') AS VALOR"
        		+ "        FROM (SELECT LPAD(NVL(FUN.MATRICULA,FUN.CODFUNC),15,'0') AS MATRICULA,"
        		+ "                   SUM(VAL.VLRVALE * VAL.TOTADIAS) AS Valor"
        		+ "              FROM TFPFUN FUN"
        		+ "        INNER JOIN AD_TCALCBENEFVALES VAL ON VAL.CODFUNC = FUN.CODFUNC AND VAL.CODEMP = FUN.CODEMP"
        		+ "        INNER JOIN TFPLIN LIN ON LIN.CODLINHA = VAL.NULINHA"
        		+ "             WHERE LIN.CODLINHA = 10"
        		+ "				AND VAL.REFERENCIA =  {REFERENCIA}"
        		+ "          GROUP BY FUN.MATRICULA,FUN.CODFUNC)");
        StringBuilder conteudoArquivo = new StringBuilder();
        conteudoArquivo.append("0000101PEDIDO01.0008744139000151\r\n");
        JapeSession.SessionHandle hnd = null;
//        int iterator = 2;
        while (query.next()) {
            String nsr = query.getString("NSR");
            String tr = query.getString("TR");
            String matricula = query.getString("MATRICULA");
            String valor = query.getString("VALOR");
            
//            conteudoArquivo.append(String.valueOf(String.format("%08d", iterator)));
            conteudoArquivo.append(nsr);
            conteudoArquivo.append(tr);
            conteudoArquivo.append("          ");
            conteudoArquivo.append(matricula);
            conteudoArquivo.append(valor);
            conteudoArquivo.append("\r\n");
//            ++iterator;
        }
        
        
        String chave = "rio_card_" + UIDGenerator.getNextID();
        hnd = JapeSession.open();
        EntityFacade dwfEntityFacade = EntityFacadeFactory.getDWFFacade();
        byte[] fileContent = conteudoArquivo.toString().getBytes();
        SessionFile sessionFile = SessionFile.createSessionFile((String)nomeArquivo, (String)"text", (byte[])fileContent);
        ServiceContext.getCurrent().putHttpSessionAttribute(chave, (Serializable)sessionFile);
        contexto.setMensagemRetorno("<center><a id=\"alink\" href=\"/mge/visualizadorArquivos.mge?chaveArquivo=" + chave + "\" target=\"_blank\">Baixar Arquivo</center>");
    }
}

