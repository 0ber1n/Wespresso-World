package io.wespresso_world.wespresso_world;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

@Component
@ConfigurationProperties(prefix = "flag")
public class FlagConfig {

    private String sqli          = "wes{$ql1_1nj3ct10n_w1ns}";
    private String idorCart      = "wes{1D0R_3xp0s3d_4dm1n_c4rt}";
    private String jwtNone       = "wes{n0n3_$hall_p@55}";
    private String fileUploadExtOnly  = "wes{file_upload_double_extension_bypass}";
    private String fileUploadEndswith = "wes{file_upload_endswith_bypass}";
    private String fileUploadMagicByte = "wes{file_upload_magic_byte_bypass}";
    private String fileUploadCdr = "wes{file_upload_polyglot_cdr_bypass}";
    private String xxe           = "wes{xxe_3xt3rn4l_3nt1ty}";
    private String ssti          = "flag{th1m3l34f_ssti_pwnd}";

    public String getSqli()               { return sqli; }
    public void setSqli(String v)         { this.sqli = v; }

    public String getIdorCart()           { return idorCart; }
    public void setIdorCart(String v)     { this.idorCart = v; }

    public String getJwtNone()            { return jwtNone; }
    public void setJwtNone(String v)      { this.jwtNone = v; }

    public String getFileUploadExtOnly()        { return fileUploadExtOnly; }
    public void setFileUploadExtOnly(String v)  { this.fileUploadExtOnly = v; }

    public String getFileUploadEndswith()       { return fileUploadEndswith; }
    public void setFileUploadEndswith(String v) { this.fileUploadEndswith = v; }

    public String getFileUploadMagicByte()       { return fileUploadMagicByte; }
    public void setFileUploadMagicByte(String v) { this.fileUploadMagicByte = v; }

    public String getFileUploadCdr()      { return fileUploadCdr; }
    public void setFileUploadCdr(String v){ this.fileUploadCdr = v; }

    public String getXxe()               { return xxe; }
    public void setXxe(String v)         { this.xxe = v; }

    public String getSsti()              { return ssti; }
    public void setSsti(String v)        { this.ssti = v; }
}
