package com.hackovation.hybo;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import javax.net.ssl.HttpsURLConnection;

public class HttpURLConnectionExample implements Runnable{

	public HttpURLConnectionExample(String fileName, String stocks) {
		this.fileName = fileName;
		this.stocks = stocks;
	}

	private final String USER_AGENT = "Mozilla/5.0";
	String fileName;
	String stocks;
	

	public static void main(String[] args) throws Exception {
		ExecutorService executorService = Executors.newFixedThreadPool(4);
		Map<String, String> fileNamesAndStocks = new HashMap<>();
		String CRSP_US_Total_Market="AAPL,MSFT,AMZN,FB,JNJ,XOM,JPM,GOOGL,GOOG,GE,T,WFC,PG,BAC,BRK.B,CMCSA,CVX,PFE,VZ,HD,PM,MRK,V,KO,C,INTC,UNH,PEP,DIS,CSCO,MO,ORCL,IBM,MCD,MMM,WMT,MA,BRK.A,MDT,AMGN,BA,ABBV,HON,SLB,AVGO,PCLN,BMY,UNP,CELG,SBUX,UTX,GILD,QCOM,TXN,USB,COST,LLY,ABT,CVS,ACN,GS,DOW,AGN,WBA,NVDA,TWX,UPS,NKE,ADBE,CHTR,LMT,LOW,MDLZ,TMO,DD,CL,NFLX,NEE,CB,AIG,CAT,PYPL,CRM,DUK,AXP,MS,PNC,RAI,KHC,AMT,COP,BIIB,DHR,GD,EOG,MON,AET,D,CSX,SO,AMAT,SPG,ANTM,RTN,BK,TJX,FDX,SCHW,KMB,NOC,PRU,OXY,MET,ADP,F,YHOO,GM,BLK,BDX,SYK,CI,ATVI,TSLA,CTSH,MMC,JCI,ITW,DE,EMR,CME,PX,HAL,COF,SPGI,BSX,CCI,ESRX,NSC,REGN,ICE,KMI,ISRG,EBAY,AEP,LUV,EA,TRV,ECL,ETN,PCG,HUM,MCK,AON,INTU,EQIX,BBT,PSX,EXC,GIS,DAL,MU,WM,PSA,HPQ,ALL,APD,ADI,HPE,TGT,MAR,AFL,VRTX,ZTS,STT,PLD,SRE,BAX,FOXA,STZ,PXD,APC,FIS,TEL,KR,VLO,MPC,PPG,PPL,GLW,FISV,EIX,SYY,CMI,SHW,HCN,AVB,STI,ILMN,WDC,LYB,NWL,LRCX,ED,ROST,WY,PGR,EW,YUM,XEL,HCA,CCL,EQR,ZBH,ADM,DLPH,ADSK,WMB,VTR,LVS,BHI,CAH,IR,ROP,APH,PEG,DFS,ORLY,BCR,PCAR,AAL,DXC,SYF,IP,MTB,MNST,CBS,SWK,PH,EL,ALXN,ROK,DG,UAL,WEC,SWKS,ES,DTE,OMC,INCY,TMUS,A,FTV,CERN,MCO,PAYX,WLTW,RCL,KEY,NTRS,DLR,CXO,SYMC,HIG,BXP,AMP,NUE,NEM,PFG,MCHP,ULTA,FITB,APA,EXPE,COL,K,CFG,DLTR,CLX,NOW,AZO,LVLT,RF,DPS,DVN,SBAC,ESS,CAG,MYL,WCN,XLNX,TAP,VMC,MJN,EFX,MGM,KLAC,TROW,RHT,INFO,BBY,HSY,VNO,TSN,DVMT,MTD,RSG,MHK,BMRN,DGX,ABC,FCX,IDXX,O,HCP,XRAY,LNC,HSIC,VFC,LH,BLL,MLM,BEN,ETR,FRC,SJM,AME,HRS,GPN,AWK,WHR,GGP,HES,AEE,MSI,NLSN,WAT,DISH,CMG,CTL,WRK,HBAN,MXIM,L,CHD,HST,CMS,ADS,HLT,GPC,MKL,COH,LBRDK,FE,STX,DOV,IVZ,ALB,TDG,FLT,LB,NBL,WDAY,FAST,NOV,Q,CTXS,NLY,CE,VRSK,HOLX,VIAB,CMA,ARNC,MAS,CNC,MKC,LLL,HAS,KMX,CNP,TXT,DHI,XL,MAA,CPB,ACGL,SNPS,EMN,BG,NTAP,WFM,DRI,MRO,FNF,CINF,PNR,ANSS,FOX,ALK,COO,WYN,JNPR,CTAS,TWTR,WYNN,DVA,OKE,LEN,ALGN,ARE,LEA,PANW,UDR,COG,UNM,TIF,IT,XEC,DRE,UHS,DPZ,SLG,KSU,AJG,VNTV,FMC,RMD,CBG,CA,PRGO,TRGP,AMTD,QRVO,IFF,TSS,PNW,CDNS,IPG,QVCA,TSO,FBHS,LNG,CDW,PKG,EXPD,EQT,ETFC,CHRW,LNT,SNA,XYL,AAP,HOG,RJF,MSCI,EXR,VAR,LKQ,ARMK,URI,WU,BWA,TRMB,HII,TFX,BR,BF.B,AMD,CDK,HRL,SIVB,UGI,FRT,ALKS,REG,SCG,ATO,IRM,ALLY,GWW,Y,SEE,MTN,CSGP,TMK,COTY,AMG,NI,PVH,FANG,LDOS,SPLK,FFIV,JAZZ,IEX,MAC,JKHY,INGR,ZION,GT,HDS,VAL,CCK,AOS,VER,NVR,RGA,NCLH,MOS,LUK,EWBC,STLD,FL,MAT,VRSN,NDAQ,HBI,IAC,RE,TTWO,AKAM,AES,WCG,JBHT,JBLU,WR,ALLE,CGNX,BERY,CLNS,LSXMK,SIRI,AVY,SBNY,AGNC,MIDD,PF,CC,CPT,GLPI,RPM,TSCO,LII,M,AYI,SEIC,COMM,KRC,TER,WOOF,OGE,WST,KIM,KEYS,WAB,VMW,TTC,CBOE,OC,LEG,SRCL,AFG,WPC,ST,PKI,BURL,MAN,MKTX,ANET,PE,KSS,LW,AXTA,AIV,ARW,PNRA,PTC,SPR,ELS,STE,MRVL,FDS,CIT,CSL,PHM,SSNC,VOYA,NFX,ON,HUBB,EVHC,JEC,DCI,FLS,WBC,WRB,CF,ACC,FLR,VEEV,GXP,GGG,OHI,COHR,XRX,ALSN,ULTI,MIC,CPRT,SUI,AA,LPT,KAR,BIVV,NYCB,OA,SGEN,SPLS,LOGM,ODFL,HTA,RNR,XPO,FDC,WTR,LAMR,FTNT,LAZ,DISCK,STWD,RRC,SCI,TOL,PBCT,ABMD,AXS,TYL,MSCC,NDSN,DXCM,NNN,RHI,BAH,PACW,TSRO,IONS,TRU,EGN,DEI,FCE.A,HRB,BRX,GNTX,AIZ,UTHR,NUAN,GPS,OZRK,DNKN,ALNY,SNI,EXEL,ATR,ARRS,MDU,LECO,ATHN,CFR,RGLD,ZBRA,JLL,FLIR,BRCD,HP,HIW,KORS,NWSA,ZAYO,SERV,MD,AGO,TGNA,VVC,S,JWN,HPP,NRG,SON,HRC,RS,EV,SNH,ACM,FWONK,BC,NRZ,ORI,CSRA,BBBY,JBL,POST,GRMN,AMH,CBSH,GRA,GWRE,BRO,OLN,DCT,HUN,IPGP,RSPP,OLED,FAF,NCR,HPT,SNV,PII,AVT,WFT,OSK,HXL,POOL,HLF,AZPN,CAVM,MPW,BIO,HHC,CPN,LULU,NFG,LYV,VVV,CASY,THO,AR,MNK,Z,CLB,EPR,PVTB,CUBE,CHK,WBS,CONE,SLM,NATI,GPT,PWR,NEU,SIX,IDA,THS,MKSI,NAVI,LVNTA,AGCO,CRL,AWH,SABR,RLGY,BWXT,EXP,EEFT,WAL,CNK,TRIP,CY,WPX,WSO,ATH,HFC,WGL,CRUS,DFT,ROL,ALR,GPK,CTLT,SNAP,POR,EPC,TECH,TDY,PB,BMS,ASH,FICO,PRXL,UNIT,EPAM,FNB,DISCA,EXAS,MMS,GWR,VR,CBRL,MUR,USFD,APLE,FHN,MASI,BLKB,IBKC,WEX,MDSO,ISBC,SMG,HBHC,TRN,HR,CR,DST,JCOM,EQC,BPOP,BFAM,DNB,WSM,HLS,RL,G,UBSI,MSG,NUVA,CRI,NBIX,WWD,MSM,EME,LOPE,CLGX,UMPQ,GRUB,BKH,OGS,SAVE,PDCO,ALE,OI,WRI,CST,LFUS,GEO,TCBI,LSXMA,ASB,NJR,HAIN,CW,MTG,AL,HE,X,MPWR,SWX,RAD,CUZ,VSAT,COR,SXT,CNO,WTFC,R,PTEN,RBC,THG,TDC,RIG,PFPT,UAA,CLR,TCO,ELLI,SRC,LSTR,ENTG,LSI,FLO,TWO,STOR,BKU,RYN,RDN,SQ,DKS,SHO,TECD,DATA,KEX,CXW,FR,VSM,JACK,ITT,SIG,UA,SNX,OFC,BOH,CIM,CIEN,ENR,DLX,PK,PRI,LITE,HCSG,MANH,VC,CABO,SFR,MFA,MBFI,IDTI,RICE,GDDY,CFX,LM,SKX,TKR,ESRT,LHO,ENS,LPX,NTCT,KITE,CHFC,PRA,OUT,SPB,CNDT,CHE,WTM,UMBF,VMI,LPLA,TEN,TEX,AMCX,WEN,SFM,TXRH,KMT,SLCA,PDM,SR,BLUE,ACHC,DOC,POL,PNM,AEIS,BDN,DAN,AHL,NKTR,FULT,SWN,IART,CLH,FSLR,CACI,NWE,BDC,ERIE,STAY,WCC,SLAB,SIGI,HOMB,WNR,UNVR,CBT,PAYC,AKRX,RPAI,TUP,BECN,CAB,INCR,PRAH,SJI,B,ESL,NHI,ILG,WAFD,SUM,PGRE,HEI.A,NUS,TRCO,BRKR,BCO,LNCE,VWR,VLY,LEXEA,PNFP,FIVE,AGR,PLAY,EDR,BXMT,MTZ,PDCE,RHP,AN,IDCC,PBI,ZNGA,DLB,WMGI,SF,AVA,MOH,MSA,STL,DORM,FNSR,SANM,JBT,ODP,BGS,ACIW,WBT,MDCO,DDR,SAIC,PBH,CATY,CXP,GHC,ENDP,TDS,ASGN,TTEK,WAGE,AAN,CMD,BGCP,ITRI,CBU,VIAV,DAR,EGP,VRNT,SBH,WLL,FUL,BKD,NXST,HA,KLXI,ESNT,VAC,MTX,CAKE,DY,MTSI,CAA,SMTC,SKT,BWLD,BCPC,USG,ACAD,NGVT,SBGI,FCFS,INT,GBCI,HELE,SLGN,FII,WWW,PAH,MIK,LPNT,BXS,WRE,PODD,RLJ,FFIN,HAWK,IRBT,PSB,QEP,PBYI,OII,MUSA,LANC,WLK,ANAT,NBR,BID,ORA,KATE,RXN,IRWD,RGC,CACC,PFGC,LGF.B,HUBS,GATX,CNX,JW.A,CREE,OAS,RLI,TCF,ROLL,FEYE,UE,SSB,SAFM,AIT,TPX,NEOG,PSMT,HRG,CVLT,UFS,CPE,ABM,ZG,HL,NYT,MOG.A,MGEE,GME,IBKR,HOPE,PEB,ZEN,PEN,CHDN,GWB,DRH,PZZA,GPOR,DECK,STAG,GNRC,SAGE,DOOR,CCP,LBRDA,EFII,AKR,VSH,EE,UNF,CHH,DDD,W,JHG,BIG,HQY,RH,UHAL,CMP,ROIC,CLI,SHOO,GCP,ONB,KNX,COLB,MDP,AXE,HGV,FNGN,HAE,STMP,MDRX,EVR,EVER,PBF,GMED,AEL,CMC,CVBF,VGR,PTLA,LCII,BUFF,QTS,ABCO,CLVS,CVG,CMPR,BLMN,OPK,SEMG,HI,TIVO,FMBI,PAY,BLD,LGND,INVH,RARE,LAD,UNFI,PEGA,FCNCA,BYD,ATGE,OLLI,P,YELP,SYNA,CACQ,TRMK,WBMD,MATW,LC,RP,JJSF,OMI,KBR,ACXM,DGI,MTDR,H,AKS,EAT,HNI,XHR,CPS,LTC,CTB,SWFT,CBI,ESV,MLHR,CSOD,TREX,SM,AEO,HEI,AGIO,POWI,WOR,TPH,DRQ,ALEX,IBOC,BOKF,GVA,RNG,PCH,PLCE,AERI,HTH,JUNO,ICUI,AMBA,INN,KS,GNW,PLNT,NSR,MXL,ROG,BRKS,REXR,KW,MWA,ALGT,TWOU,ICPT,IVR,CCMP,SUPN,DBD,SATS,MRCY,DNOW,CAR,CDEV,AGII,PLT,KWR,PLXS,PINC,NWN,CBM,UCBI,CVA,AMN,WTS,EGBN,AWI,CFFN,MSTR,LXP,NVRO,SCL,LPI,BANR,CRS,UFPI,IIVI,FDP,AF,ARI,SKYW,SITE,ATI,CDE,UVV,HYH,AWR,RNST,EXLS,CWT,GDOT,KFY,MMSI,DF,NWS,SSD,MRC,BRC,MLI,TOWN,FRME,CCOI,PRAA,HZNP,CLF,MORE,KBH,MGLN,FCB,QCP,SFLY,BHE,PEGI,PCRX,FWRD,OTTR,SPN,CATM,APFH,INDB,WSBC,MCY,SRPT,FGEN,HMSY,AAT,NTRI,KCG,HMN,GOV,FTR,URBN,TGI,MNRO,GEF,SFBS,PRLB,APOG,ESGR,MTH,KN,ABCB,NBTB,RDC,COLM,AMED,FELE,OIS,KRG,FLOW,MDR,NEWR,SIR,GNL,CORE,AVP,XPER,NWBI,BNCN,ARCH,TTD,TRNO,FCPT,FFBC,NSIT,IPHI,KMPR,ENV,UBSH,IOSP,FCN,IMPV,ATU,SBRA,PENN,RRR,PAG,EXPO,CALD,OSIS,INGN,LTXB,VECO,SFNC,AAON,LGF.A,NYRT,SHEN,MDXG,INFN,TBPH,NPO,WDFC,WPG,AZZ,ESE,SEM,OCLR,NXTM,HALO,MEI,FOE,AIN,PFS,BOBE,MDC,TTMI,MYGN,OMCL,WSFS,AJRD,THRM,BHLB,JCP,MTOR,TMHC,KALU,DK,CRZO,VG,PRGS,NSP,FHB,RDUS,TILE,RGEN,TWNK,THC,NTGR,APAM,ETSY,WDR,RMBS,CHSP,PRK,NAVG,CBL,SRCI,JELD,KAMN,NP,BLDR,RWT,SCS,SCSS,MSGN,LZB,HMHC,SYKE,VREX,FIX,AMWD,ARRY,TNET,CYS,AOBC,CNMD,EGOV,NAV,BOFI,SAM,MORN,AXL,EBIX,TWLO,MATX,WABC,WD,GIMO,WNC,MINI,FN,BMCH,TNC,RES,ITGR,CSGS,GPI,RAVN,EIG,OMF,SGMS,BSFT,ACCO,COUP,CTRE,ATSG,PCTY,QTWO,SC,AYR,QLYS,ELY,VSTO,HSC,AAXN,GBX,SEAS,SEB,GRPN,WERN,EGHT,ONCE,SPNC,SAIA,LABL,UBNT,AAWW,CHS,WSTC,PPC,CSFL,PPBI,STRP,CPK,TRTN,FSP,BPFH,FOLD,AAOI,AIR,SWM,XON,LQ,ADC,CUB,HUBG,HF,TERP,IPXL,SXI,FIZZ,BKFS,TVTY,CAL,TIME,NYLD,PMT,KRNY,STBA,WETF,TREE,FOXF,WEB,EBS,BPMC,ASTE,RGR,TRUE,FCF,SONC,ECOL,AMC,TMP,BMI,TBI,FET,AVXS,TDOC,FIVN,RUSHA,ABG,AIMC,SPTN,BABY,HASI,PLUS,CIR,CALM,COKE,MBI,GTLS,NOVT,HTLF,NGHC,NSA,BCC,EXTR,FORM,COTV,HSNI,CBF,ABAX,PNK,DSW,SVU,EVTC,TPC,ERI,IBP,CMO,LKFN,BANC,IPCC,BEL,STC,CVGW,KNL,BNCL,CPLA,KRA,PATK,RPT,SPSC,BRKL,TRS,USCR,NE,MNTA,ROCK,EVH,SNHY,MANT,CENTA,BUSE,CBPX,SSP,BGG,EPAY,PRIM,SCSC,SPXC,CNSL,SAFT,CYH,INSM,CKH,ARR,SMCI,AMKR,AMSF,SMP,CVCO,CORT,FSS,DIOD,FCH,RMAX,MNR,ADTN,CSII,LHCG,INVA,RRGB,ADSW,MB,DYN,UNT,NCI,SRG,HRI,DDS,SCHL,PSTG,FBP,HURN,MCRN,SSTK,CHCO,EXTN,CEVA,VVI,AFSI,ALOG,SBSI,UEIC,ANF,GCI,TROX,HTLD,ANDE,STBZ,GKOS,CPF,BJRI,TFSL,MC,ENSG,STRA,FNFV,BOX,UHT,UFCS,SASR,GDI,GMS,SBCF,MTSC,LNN,GBT,LADR,XOG,JOE,GNCMA,ISCA,ICFI,PJC,INOV,ALX,AXDX,LBAI,GTT,DERM,MTGE,ASIX,ALG,PKY,ECPG,DENN,MYCC,MTCH,CHGG,LDL,UVE,SHLM,WING,STAR,XLRN,UBNK,LOXO,HAFC,WIRE,TYPE,AGX,LTRPA,OXM,CNS,IRDM,LSCC,BATRK,FIT,GLT,OSUR,KAI,XNCR,LMNX,KND,RRD,DIN,SJW,PRO,SNR,EBSB,VRTU,IPHS,LL,BEAT,WIN,FWONA,BGC,GPRE,TIER,GSAT,BCOR,TPRE,CBZ,GFF,KTWO,ATW,QSII,FPRX,GIII,IMMU,CASH,GTN,WTW,FFIC,ATRI,MGRC,NBHC,RYAM,USPH,CUDA,AMBC,CUBI,DPLO,DEL,FMSA,BANF,NNBR,HT,CCF,MSFG,WMS,SYBT,QUOT,WGO,KTOS,MHLD,CLW,GOGO,OCFC,NNI,CLDT,CRAY,MOD,ACIA,PMC,EFSC,PHH,TISI,CBB,DO,IRET,REN,FIBK,CCC,GES,BAS,RTEC,NX,HSTM,KOP,WWE,MTW,PEI,USNA,OFIX,AROC,DEA,GCO,MHO,ZIOP,UVSP,JRVR,AFAM,GPRO,BMTC,CENX,IBTX,KELYA,PLAB,AVX,ATNI,NWLI,PETS,HLX,HMST,MTRN,NYMT,HFWA,QUAD,TCBK,UCTT,NFBK,EGRX,HIFR,BFS,ETH,UTL,TRST,SGYP,LKSD,PI,HLI,IRT,ORBC,DHIL,ALDR,ATRC,AKAO,TTS,ATKR,NANO,VCRA,NEWM,CNOB,CTS,BRSS,WASH,RUTH,QDEL,AVAV,PAHC,TXMD,PLOW,LRN,VRTS,NCS,CYTK,KNSL,SHAK,DEPO,ORIT,CZR,ANIK,ACLS,GTY,MGPI,GABC,SD,AEGN,HEES,ACOR,CAMP,LCI,KBAL,PFBC,RESI,PJT,FFG,NHC,CTBI,RPXC,ITG,OMER,CECO,XENT,FLIC,HRTX,VNDA,DNR,GLRE,GBNK,FPO,NVCR,THR,RTRX,FBNC,SP,ATRO,IPAR,ESND,NR,NYLD.A,DFIN,AMAG,MCS,TTEC,CWH,PRFT,UIS,CAC,AMPH,COBZ,SYNT,PSTB,VRNS,GHDX,WIFI,NMIH,VIVO,AHH,EPZM,FARO,ANH,HSKA,CHRS,EXPR,SGBK,INSW,LXRX,RCII,IIIN,CUNB,FBC,FINL,BDGE,DCOM,GNMK,NLS,MRTN,REX,PRSC,SXC,CSWI,MPSX,DXPE,HY,UBA,MGNX,CTWS,NPK,SNC,LGIH,HTZ,PARR,CASS,MSEX,KEM,GSBC,QCRH,MPAA,REI,CRY,CWST,SNDR,TR,WLH,HBNC,FTK,NEO,FRGI,JBSS,PGTI,INO,WAIR,LAUR,ALRM,PEBO,SNCR,BL,BW,GNC,TLRD,USM,ACHN,MITT,AGM,INTL,IMKTA,HIBB,GOOD,LORL,KERX,PCBK,ESPR,INWK,AHT,LJPC,ELF,AMRI,XCRA,ANGO,TSC,LPSN,KMG,BKE,PRTY,MLAB,CHUBK,SCHN,ECHO,FRED,WMK,VRTV,HTBK,EGL,MBWM,CMCO,EIGI,CATO,MYE,FRAN,NSM,CROX,FRAC,CBS.A,GHL,WSBF,TGTX,SUP,HZO,HOFT,RATE,APTS,LOB,COHU,EVBG,FMI,ANGI,SAH,GBLI,ENTA,AIMT,SGMO,FOR,KE,HDP,HWKN,TWI,TOWR,MYRG,HAYN,LION,LDR,PGNX,FSB,CRVL,AVD,FDEF,RVNC,TG,EVRI,FOSL,ACTA,CTT,KFRC,RGNX,PGC,OKSB,SCLN,SPWR,UFI,PDFS,TMST,ARCB,LNTH,MBUU,WHG,NCMI,CHUY,CPSI,REVG,CTRL,ABTX,IVC,MDCA,CALA,FRBK,LMAT,BNFT,YORW,GERN,ACRS,FISI,SGRY,GOLF,INSY,UMH,BHB,CORR,VIRT,THFF,JAG,XOXO,WK,EZPW,MGI,VDSI,AFI,HVT,AKBA,IBCP,OFG,SPPI,KEYW,BHBK,SRI,PTCT,GRC,BCRX,NXRT,WMC,CARB,CDR,AROW,ACET,MSBI,CRC,CCS,TAST,LEN.B,WSR,IMGN,ABR,TACO,LMOS,FFWM,HTBI,DFRG,ARNA,FLXN,ALJ,ENVA,PDLI,SHOR,FORR,MED,CARA,CSU,BBSI,REGI,EBF,SN,BZH,GNBC,ATEN,INST,NVEC,HSII,VIA,ELGX,EVC,HLIT,FLXS,ENZ,PIR,VSAR,OB,IXYS,MOFG,CSV,ASMB,ANCX,SSNI,OMN,FCBC,AXGN,RUN,HCI,GEF.B,BKS,FGL,MMI,BKMU,COWN,STFC,LNDC,CCRN,NCBS,VSEC,MG,EDIT,GTS,CARO,PICO,HABT,CFI,PLUG,RECN,ANIP,BH,FARM,OME,TLGT,CIO,AOSL,MRT,FRP,UCFC,DAKT,CVI,WRLD,HRTG,BV,VBTX,PUB,MCRI,PRTK,SPOK,AI,ATRS,ACRE,HK,FBNK,APEI,HCKT,KPTI,PGEM,RMR,FND,SFS,ITCI,RMTI,TRR,TTI,BMRC,TRUP,DMRC,FMNB,TBK,HZN,JIVE,BNED,CLDX,NUTR,RGS,ASNA,NCOM,MOV,OSBC,RXDX,PKE,NPTN,PFSI,ARAY,RBCAA,CMTL,EXAC,PKOH,MPO,SCMP,DX,NGS,FLWS,DCO,EHTH,NTNX,KRO,WTBA,CHEF,CSBK,VEC,SONS,ACBI,ORC,OPB,CNXN,RPD,FRPT,QADA,SRDX,CHCT,CENT,GPX,TBBK,MCFT,BSET,ICON,CDZI,FPI,OLP,FF,EEX,SRCE,MEET,TITN,ZIXI,CAI,IDT,ADXS,PDVW,TRC,FRPH,PHX,MLR,YRCW,BLCM,BGFV,HOV,LOCO,CCNE,PBPB,CLNE,AMSWA,BOJA,WFBI,OKTA,AXAS,NVEE,AMNB,RSO,HONE,ZGNX,SNOW,FTD,POWL,GLYC,UIHC,BETR,HDSN,ONVO,SNBC,BRG,RIGL,NLNK,WINA,OTIC,RDI,ADUS,VSI,VICR,PACB,AHP,ARII,NXEO,ARTNA,HCOM,HCC,MODN,CECE,PRMW,FFKT,LBIO,CRAI,CALX,CRMT,XTLY,NSTG,AT,ADRO,IRTC,NTRA,FMBH,EMKR,NKSH,ECOM,SPAR,ZUMZ,CRCM,CLDR,BSRR,NVTA,CONN,SCVL,WLDN,ATLO,INAP,RFP,PFIS,ICHR,STAA,CUTR,ESIO,QTM,CIA,CTRN,ERII,IVAC,ZOES,CZNC,BPI,CTMX,WNEB,TTPH,GLUU,CLD,ADMS,FBK,RDNT,NVAX,TBNK,APPF,BBG,XBKS,EQBK,TNAV,TRNC,IMMR,STS,FNLC,CACB,AGEN,SPKE,SGA,STRL,NC,ETM,EBTC,DGII,AXTI,BLMT,CERS,WATT,RST,MCBC,CIVI,RUSHB,MITK,BOLD,OSTK,OCN,DSPG,BATRA,CHUBA,FBMS,KREF,CSS,ATRA,OLBK,EMCI,CHFN,SLD,GLDD,UTMD,PERY,MEDP,JOUT,AVXL,SREV,LE,HIIQ,TCMD,MACK,HIFS,SIGM,SFE,ZAGG,MYOK,MRLN,BELFB,BSTC,PEIX,HCCI,AJX,SFST,CRBP,OCUL,MBTF,CVTI,LIND,GRBK,WRD,MOBL,OFLX,KODK,WMIH,CVCY,CTO,BTX,ASPS,LCUT,TRK,DVAX,RTIX,RNET,LYTS,LMNR,CNCE,TREC,SPWH,RM,AC,PBNC,BWINB,RUBI,RAIL,LTS,KOPN,MTRX,SENEA,SYX,RAS,CVLY,LLNW,TESO,NRIM,IDRA,NHTC,HBCP,DJCO,GDEN,HIL,PETX,SHBI,AVID,NERV,RLGT,NRCIA,RILY,VPG,GCAP,DLTH,BFIN,SHLD,SND,NEWS,CVGI,VLGEA,ADES,CHMI,REV,FNHC,CORI,RRTS,HMTV,CCN,ITIC,GHM,AGFS,DGAS,CRIS,GEOS,FC,CCXI,ZYNE,CLCT,GORO,RARX,ACTG,CCBG,SONA,AMOT,DSKE,HCHC,ROX,QTNA,ORN,VRA,VCYT,CMRX,EXA,MFSF,DS,EDGE,MCRB,IPI,NATH,HURC,BCOV,RICK,KEG,EPM,PES,CEMP,FBIZ,WAAS,UEC,FNWB,STRS,USAT,UFPT,PWOD,TTGT,PMBC,MXWL,BWFG,PKD,LCNB,EVBN,SMBC,TWIN,ECR,ARA,BOOM,SMMF,SNDX,CRR,JCAP,TPIC,CNBKA,CNTY,LXU,WEYS,IMH,MNOV,HOME,LBY,CASC,CDXS,SGC,ISTR,STML,PFBI,ERA,GST,DGICA,ODC,CIVB,SIFI,ITI,BSF,BOCH,VHC,AP,ACNB,EML,FCFP,BREW,AFH,SPA,BIOS,SMBK,ORM,WMAR,AVHI,ASUR,INBK,UPLD,EPE,CFFI,ENT,ORRF,OSG,ASCMA,FONR,LQDT,CMT,ZEUS,DRRX,COLL,USLM,PLSE,PCMI,CYBE,SBCP,AGYS,REIS,CPRX,BRS,SELB,FSTR,EGLE,CLFD,BLBD,ANAB,LMIA,ENTL,PCYG,ENFC,BBW,NTLA,PPHM,CUBN,TSBK,JAX,LAWS,KVHI,CFMS,HDNG,MCF,HNRG,ONDK,GMRE,AQMS,FRTA,RSYS,TIPT,GENC,NWPX,CRD.A,KIRK,SLP,CCO,MLVF,NEFF,TUSK,GBL,AMBR,IHC,FOGO,PLPC,HALL,ENOC,OPY,DHX,SGM,GUID,ESSA,NWHM,ARDX,ATHX,BCBP,TIS,QNST,IESC,PRGX,NBN,EARN,NATR,HBP,YUME,MVIS,KINS,PCYO,OOMA,MSL,NCIT,ABEO,TPHS,USAP,XONE,RT,PLPM,MLP,CBAY,JNCE,YEXT,TAX,BDE,AMRC,PBIP,RYI,STRT,CHMG,KURA,WG,CNAT,ICD,GIFI,ICBK,ARC,GNCA,PXLW,SQBG,EGAS,PZN,VYGR,BBOX,ULH,ABTL,PKBK,NSSC,MNKD,UNTY,SRT,WLB,LAYN,NEOS,KIN,DLA,TLYS,VOXX,VRS,APTI,PROV,CFCB,BDSI,TRCB,BGSF,ESCA,SLCT,CLSD,HIVE,I,GSIT,BCRH,REPH,XBIT,DNBF,VVUS,TPB,ALSK,CSLT,GNE,XXII,QHC,FFNW,FGBI,FATE,SHLO,IMDZ,FCCY,BOOT,AHC,FBRC,PHIIK,PMD,CHKE,FSBW,SBBX,HWCC,GV,PCTI,NGVC,ALCO,RNWK,FLDM,MNTX,RLH,LEE,LAND,RVLT,III,TCS,WTI,SIEN,FBIO,ECYT,PFSW,SYNC,AIRG,CBFV,RCKY,MPX,QUIK,NVTR,VCEL,SYRS,WLFC,ESTE,AREX,HFBC,IVTY,CRD.B,SYNL,ARWR,RBPAA,NK,TSQ,FUEL,PAR,PVBC,NAME,HLTH,ADMP,ACFC,LFGR,CTSO,SCWX,ISRL,TESS,SRNE,INSE,ARIS,CYTR,ACU,RNN,MCHX,INFI,DXLG,TRVN,CRVS,XRM,CRK,AOI,VIVE,PCO,CLUB,AE,PTGX,SPNE,GWRS,SALM,SOHO,AMRB,MEIP,UCP,GCBC,EDGW,IPAS,SEAC,MSBF,KDMN,VTL,JMBA,OPHT,TRNS,CDXC,JONE,CTIC,CIE,ASYS,CPSS,LINC,ACRX,CDI,MRAM,ALOT,PFMT,CWAY,RNDB,CUI,BTN,PFNX,ARQL,IRIX,CTG,GSB,AEHR,ASRV,COVS,LAKE,DWSN,DWCH,ZFGN,RELY,SAMG,PSDV,JAKK,TUES,BKJ,OMED,WKHS,FSFG,UTI,ATNM,VRAY,SBFG,SNAK,URG,NLST,BWEN,VSLR,NAVB,CYRX,CDTX,GEC,HNH,PBBI,KTCC,ADVM,ALIM,DRAD,CEMI,SSFN,ASPN,ABCD,VSTM,IDSY,INVE,WHLR,SPHS,SPAN,SYN,AVEO,NOG,PRCP,CPIX,NRCIB,AAC,TRMR,KALV,WBB,MN,WSTG,NTIC,AWRE,AVNW,EVLV,RELL,FEIM,TZOO,CRWS,SBPH,AST,AIQ,NDLS,AMSC,LPCN,HBIO,UFAB,SFBC,SITO,AMRK,BCLI,CZWI,MSON,VUZI,AXSM,FUSB,MDGL,ARTX,MLNK,AGTC,AAMC,TACT,RMCF,CVV,HHS,ULBI,CVU,SNSS,VIRC,CGIX,NNVC,MFNC,KTEC,LPTH,BSQR,AGRX,WCST,NVIV,NH,CCUR,ALDX,SSI,INTT,XCO,EBMT,ZN,ICAD,OAKS,NAII,VISI,TTOO,HNNA,UBCP,DXYN,GEMP,LRAD,PGLC,SANW,OBLN,MRTX,PBSK,CVRS,DAIO,NL,EGY,CGI,FRSH,PRTS,IIPR,UONEK,TRHC,ANCB,HTM,TRCH,MATR,ALJJ,HOS,BBRG,RGLS,APPS,WEBK,FCEL,COOL,CLIR,GEN,KMPH,NEON,LFVN,SCX,EMAN,CSTR,LUB,KEQU,SYMX,SSBI,MNI,ATOM,NICK,LONE,DEST,SMED,NMRX,ARKR,CLRO,SKIS,SNFCA,ZSAN,IIN,NTIP,AMPE,LWAY,BVX,TLF,PPIH,CUR,AINC,GALT,IMMY,BYBK,PTN,RCMT,SENS,PTSI,DRNA,PBHC,EVOL,PATI,FLL,SMRT,PTI,JNP,CVO,CTRV,ISSC,FLGT,TAYD,MIND,NWY,UG,CBK,AMRS,CFRX,LIFE,TTNP,TRXC,SAUC,BIOL,OVAS,USAK,JOB,SPRT,ESBK,VRML,FVE,NTWK,OCX,MARK,DVCR,MRVC,OTTW,PQ,IDN,HSON,XOMA,EIGR,APT,SNMX,BPMX,PESI,FRD,SELF,PMTS,EGLT,MRIN,IOTS,GVP,OREX,SKY,GAIA,METC,IO,VTVT,UQM,LUNA,DTRM,SGB,IRMD,PFIE,FLKS,CGNT,TDW,GTIM,HFBL,APVO,DZSI,INTX,IEC,AGLE,GNMX,MDLY,BCTF,OXBR,WYY,CASI,LSBK,BBGI,OESX,INSG,SUNW,SCYX,RESN,ITEK,RWC,JCS,EVI,CSPI,KONA,PTX,EMMS,DIT,RTNB,CYTX,WVVI,REED,LMB,ISR,LEU,APDN,EDUC,BPTH,STKS,SNOA,BXC,MBII,CVR,HVBC,THLD,TNDM,EQFN,UWN,ENPH,WSTL,TST,SHOS,CNFR,NOVN,PRSS,FHCO,LOAN,MTEX,INFU,BDL,EVOK,ASFI,WAC,PRKR,AUMN,CLBS,PIH,CFNB,EFOI,MLSS,EGAN,MJCO,ARCW,SGMA,ELON,JVA,REXX,ALT,BRID,LPTX,MGCD,EYES,WTT,PPSI,FENX,RBCN,AMTX,BWINA,SEV,EEI,TNXP,PN,FSAM,INTG,AXR,UNXL,XPL,ELMD,TPIV,QUMU,JASN,JYNT,CRMD,VTNR,BIOC,NSEC,PTIE,SHSP,CPST,NVLS,NVFY,WVFC,QADB,LTRX,TVIA,CIX,INUV,FSNN,CHMA,ZDGE,BLPH,HTBX,CASM,ICCC,IPWR,REFR,OTEL,DAVE,FH,AIRT,CETX,LOV,GMO,STRM,VICL,LODE,ONCS,OCRX,UAMY,YUMA,EKSO,DMTX,ONS,TCON,FCSC,SCKT,ISDR,MOC,PBIB,GROW,DDE,XPLR,OMEX,PZG,SAJA,GWGH,CYCC,DLHC,TROV,ABIO,SGY,SYPR,GALE,MRNS,HEAR,FAC,ADK,DFFN,VALU,ADMA,DGLY,ASTC,EBIO,HSGX,MAYS,FTEK,CDNA,AVIR,VBFC,CKX,OHRP,SVRA,RAVE,MSN,CBLI,IMI,BCEI,QRHC,ANTH,OBCI,NEOT,HEB,VKTX,BEBE,MRDN,CTHR,PNRG,SUMR,ESNC,SOFO,ENG,LTEA,BMRA,IBIO,AAME,VTGN,JCTCF,STLY,DFBG,SIF,AEMD,MIRN,CPSH,NAUH,RPRX,ENSV,MXPT,IMNP,ONTX,CVM,ARDM,CIDM,FNJN,RFIL,RMGN,FSBC,PFIN,ALQA,BLFS,PDEX,TORM,CUO,SNES,LENS,AMS,GTXI,TENX,DYSL,BVSN,VNCE,VSR,EYEG,NBY,ITUS,OGXI,NTN,ATEC,CATB,ISNS,AVGR,CMLS,JTPY,IZEA,CSBR,WHLM,MHH,CTIB,SDPI,GLBR,TEAR,GEVO,ESES,APRI,LINK,BSTG,RTK,SMSI,INS,RVP,NVUS,RLOG,GNUS,CLSN,MBVX,AMDA,CNXR,RKDA,SGRP,MICR,PRTO,ACY,NHLD,FALC,MNGA,PYDS,PAVM,AETI,GLA,ARGS,PHII,CAPR,AEY,AIII,POLA,CYAN,HTGM,IKNX,ISIG,IOT,NETE,ARTW,SVT,IDSA,NXTD,MBRX,ELSE,CRVP,TIK,ZAIS,JRJR,SRAX,OPGN,PLXP,PZRX,TEUM,WCFB,SLNO,CERU,AZRX,RTTR,IFMI,BONT,LMFA,MOSY,LIVE,XBIO,MSDI,NSYS,KOSS,MARA,MDVX,UONE,DXTR";
		String CRSP_US_Large_Cap_Value="AAPL,MSFT,AMZN,FB,JNJ,XOM,JPM,GOOGL,GOOG,WFC,BAC,GE,T,PG,BRK.B,PFE,CVX,CMCSA,C,HD,PM,VZ,UNH,MRK,V,KO,PEP,INTC,DIS,CSCO,ORCL,MO,IBM,AMGN,MCD,MMM,MDT,BRK.A,ABBV,MA,WMT,BA,HON,CELG,AVGO,GILD,PCLN,BMY,SLB,UTX,UNP,ABT,USB,CVS,LLY,NVDA,AGN,QCOM,SBUX,GS,NKE,DOW,ACN,TXN,UPS,TWX,WBA,COST,DD,ADBE,LMT,TMO,CHTR,LOW,NEE,CL,CB,AXP,CAT,MDLZ,MS,PYPL,NFLX,PNC,CRM,DUK,AIG,BIIB,AMT,RAI,COP,DHR,FDX,EOG,KHC,MON,SCHW,BLK,GD,SPG,BK,AET,ANTM,D,CSX,SO,MET,RTN,PRU,KMB,OXY,NOC,GM,AMAT,TSLA,TJX,BDX,F,ADP,CI,SYK,ATVI,JCI,CME,MMC,COF,ITW,DE,CTSH,ICE,REGN,EMR,BSX,ESRX,PX,SPGI,BBT,CCI,KMI,PSX,EBAY,DAL,TRV,NSC,HAL,AON,ETN,HUM,MCK,ECL,ISRG,AEP,PCG,LUV,EQIX,EXC,WM,INTU,ALL,VRTX,STT,GIS,MU,APD,PLD,EA,AFL,PSA,ZTS,VLO,STZ,FOXA,BAX,HPQ,SHW,TGT,MAR,ADI,SRE,FIS,PPG,TEL,GLW,HPE,STI,HCN,CMI,LYB,MPC,PXD,AVB,PPL,FISV,ALXN,NWL,ZBH,WDC,YUM,PGR,HCA,EIX,APC,ILMN,LVS,WY,WMB,EW,ED,VTR,CCL,CAH,SYY,SYF,EQR,DFS,ROP,ADM,DLPH,IR,IP,XEL,PCAR,BHI,BCR,LRCX,ROST,APH,MTB,CBS,AAL,INCY,PEG,DXC,KR,SWK,EL,PH,NTRS,MNST,ADSK,ROK,KEY,UAL,ORLY,DG,MCO,CERN,MYL,FTV,FITB,AMP,WEC,HIG,OMC,ES,A,DTE,BXP,RCL,WLTW,EXPE,PFG,NUE,PAYX,APA,CFG,TSN,DLR,CXO,NOW,SWKS,TMUS,RF,NEM,SYMC,CLX,LVLT,COL,K,RHT,TROW,WCN,ULTA,ESS,DPS,VMC,MCHP,EFX,SBAC,AZO,MGM,VNO,DVN,XLNX,BMRN,LH,FRC,DLTR,FCX,ABC,MHK,INFO,CAG,DGX,MTD,LNC,TAP,BEN,RSG,O,VFC,BBY,HCP,XRAY,BLL,HSY,HBAN,GGP,HSIC,KLAC,IVZ,TSO,WRK,IDXX,MSI,WHR,WAT,MLM,AME,AWK,NLSN,ETR,GPN,ADS,DISH,HST,WFM,L,COH,FLT,HRS,AEE,VRSK,HES,CE,CTL,SJM,HLT,CNC,LB,LLL,GPC,CMS,CMA,FE,CHD,MKL,Q,WDAY,HOLX,DVMT,MXIM,FAST,TDG,HAS,NOV,DOV,NBL,DHI,NLY,MAS,MAA,CMG,VIAB,COO,PANW,ALB,KMX,COG,FNF,STX,ACGL,XL,CTXS,AMTD,TXT,CINF,DRI,CNP,FOX,TIF,KSU,ALK,RMD,MKC,EMN,OKE,WYNN,ARE,SNPS,UHS,PNR,NTAP,LEN,CA,SLG,IFF,JNPR,UNM,CTAS,AMD,IT,WYN,BG,ETFC,CBG,TWTR,UDR,ANSS,RJF,CPB,PRGO,AJG,VNTV,EXPD,EQT,DVA,MRO,FBHS,ARMK,ARNC,XYL,DRE,LNG,QVCA,FMC,LEA,IPG,CHRW,LKQ,TSS,ALLY,URI,PNW,VAR,HOG,AMG,EXR,MSCI,LBRDK,SNA,LNT,FRT,REG,TRMB,HRL,CDK,BWA,WU,AYI,GWW,XEC,PVH,ZION,ALKS,BF.B,JAZZ,SEE,Y,NCLH,IRM,SCG,NVR,TMK,LUK,HBI,COTY,RGA,NDAQ,NI,MAC,FFIV,AKAM,AAP,CCK,QRVO,JBHT,VRSN,MOS,VER,WAB,SPLK,RE,AVY,SEIC,SIRI,AGNC,CPT,MAN,OC,KIM,MAT,AES,ST,M,CIT,LSXMK,AXTA,TSCO,ARW,PHM,OGE,VOYA,FDC,KSS,SPLS,FDS,WBC,JEC,CF,SRCL,FL,LW,FLR,WRB,HUBB,MRVL,XRX,MIC,NYCB,PBCT,FLS,LAZ,SNI,RHI,RRC,LULU,JLL,KORS,JWN,PII,LOGM,AXS,BRX,ALGN,DISCK,HP,GPS,NWSA,ATH,ALSN,DPZ,RS,ZAYO,GRA,S,TRGP,GRMN,AR,VMW,SIVB,GT,HII,INGR,SNAP,BBBY,TRIP,RL,UAA,DISCA,UA,EVHC,ANET,LSXMA,HRB,CLR,AA,AIZ,AGR,TSRO,WLK,NUAN,HFC,AVT,SGEN,EGN,FLIR,CLB,MNK,SIG,LBRDA,ATUS,WFT,H,PINC,NWS,INVH,AN,PPC,CBS.A,LEN.B,VIA";
		String CRSP_US_MID_CAP_VALUE="FISV,NWL,WDC,EW,ROP,BCR,LRCX,APH,MTB,INCY,ADSK,KEY,MCO,CERN,WEC,HIG,ES,DTE,RCL,WLTW,EXPE,PFG,CFG,DLR,CXO,NOW,SWKS,RF,NEM,SYMC,CLX,LVLT,COL,RHT,WCN,ULTA,ESS,DPS,VMC,EQIX,MCHP,EFX,SBAC,MGM,XLNX,BMRN,LH,FRC,DLTR,FCX,EA,MHK,INFO,CAG,DGX,MTD,LNC,TAP,O,BBY,XRAY,BLL,HBAN,HSIC,KLAC,IVZ,TSO,WRK,IDXX,MSI,WHR,WAT,MLM,AME,AWK,NLSN,ETR,GPN,ADS,HST,WFM,COH,FLT,HRS,AEE,VRSK,CE,AVB,CTL,SJM,CNC,LLL,GPC,CMS,CMA,CHD,MKL,Q,WDAY,HOLX,DVMT,MXIM,FAST,TDG,HAS,NOV,DOV,DHI,NLY,MAS,MAA,CMG,DLPH,COO,PANW,ALB,KMX,COG,FNF,STX,ACGL,XL,CTXS,TXT,CINF,ROST,DRI,CNP,TIF,KSU,ALK,RMD,MKC,EMN,OKE,WYNN,ARE,SNPS,UHS,PNR,NTAP,LEN,CA,SLG,IFF,JNPR,UNM,CTAS,AMD,IT,WYN,BG,ETFC,CBG,TWTR,UDR,ANSS,RJF,PRGO,AJG,VNTV,EXPD,EQT,DVA,MRO,UAL,FBHS,ARMK,ARNC,XYL,DRE,LNG,QVCA,FMC,LEA,IPG,CHRW,LKQ,TSS,ALLY,URI,PNW,VAR,HOG,AMG,EXR,MSCI,LBRDK,SNA,LNT,FRT,REG,TSN,TRMB,HRL,CDK,BWA,WU,AYI,GWW,XEC,PVH,ZION,ALKS,JAZZ,SEE,Y,NCLH,IRM,SCG,NVR,TMK,LUK,HBI,COTY,RGA,NDAQ,NI,MAC,FFIV,AKAM,AAP,CCK,QRVO,JBHT,VRSN,MOS,DVN,VER,WAB,SPLK,RE,AVY,ABC,SEIC,AGNC,CPT,MAN,OC,KIM,MAT,AES,ST,M,CIT,LSXMK,AXTA,TSCO,ARW,PHM,OGE,VOYA,FDC,KSS,SPLS,FDS,WBC,JEC,CF,SRCL,FE,FL,LW,FLR,WRB,HUBB,MRVL,XRX,NBL,MIC,NYCB,PBCT,FLS,LAZ,VIAB,SNI,RHI,RRC,LULU,JLL,KORS,JWN,PII,LOGM,AXS,BRX,ALGN,DISCK,HP,GPS,NWSA,ATH,ALSN,DPZ,RS,ZAYO,GRA,TRGP,GRMN,AR,SIVB,GT,HII,INGR,BBBY,TRIP,RL,UAA,DISCA,UA,EVHC,ANET,LSXMA,HRB,CLR,AA,AIZ,AGR,TSRO,WLK,NUAN,HFC,AVT,SGEN,EGN,FLIR,CLB,MNK,SIG,LBRDA,WFT,H,PINC,NWS,INVH,AN,PPC,LEN.B,VIA";
		String CRSP_US_SMALL_CAP_VALUE="PKG,CBOE,CDW,CDNS,TFX,BR,ATO,FANG,CSGP,IEX,EWBC,UGI,AOS,STLD,MTN,JKHY,WCG,LDOS,ALLE,TTWO,JBLU,WR,IAC,PKI,GLPI,SBNY,LII,KRC,BERY,CLNS,COMM,RPM,XPO,EXEL,KEYS,MKTX,WOOF,ELS,TTC,PF,WPC,CC,MIDD,SPR,CGNX,AFG,WST,LEG,STE,AIV,PNRA,SSNC,ALNY,BIVV,OHI,PE,BURL,PTC,ACC,DXCM,GXP,IONS,ODFL,CPRT,TYL,SUI,HDS,CSL,LAMR,TOL,GGG,DCI,LPT,TER,SCI,JHG,HTA,NDSN,ULTI,ABMD,VEEV,WTR,ON,UTHR,NNN,STWD,KAR,OZRK,OA,NFX,RNR,MD,DEI,BC,ATHN,PACW,FTNT,FCE.A,COHR,TRU,FWONK,LECO,IPGP,GNTX,ALGN,ATR,CFR,MSCC,HPP,ZBRA,EPR,HUN,SERV,HRC,THO,CBSH,CSRA,HIW,NRG,EV,OSK,SNV,BRCD,BRO,Z,SQ,MDU,SON,RGLD,DPZ,AGO,GWRE,DNKN,ACM,VST,OLN,ARRS,KITE,SLM,AMH,NCR,FAF,DCT,ORI,POST,CPN,PWR,TRGP,SNH,VVV,VVC,CRL,WBS,HXL,JBL,NRZ,DFT,NAVI,BIO,BLUE,NATI,WSO,MPW,WAL,HLF,LYV,THS,CONE,OLED,POOL,SIVB,PNFP,SIX,BWXT,FNB,BAH,AGCO,HPT,RLGY,NFG,CHK,GPT,HHC,PRXL,GT,AWH,TECH,RICE,ALR,NEU,EPC,EEFT,MDSO,CUBE,FICO,IDA,HII,SFR,PTEN,GPK,WGL,CY,INGR,TRN,WTFC,BPOP,BMS,EXP,WEX,PB,SABR,MASI,GWR,MUR,CASY,UNIT,EXAS,CTLT,IBKC,MTG,HBHC,EPAM,BLKB,HLS,AZPN,RSPP,CRI,ASH,FHN,CNK,POR,CLVS,MMS,TDY,NBIX,UMPQ,LVNTA,CBRL,HAIN,CR,CRUS,CAVM,SMG,ROL,DNB,HR,APLE,SKX,USFD,BFAM,EQC,VR,UBSI,ACHC,NUVA,JCOM,OI,PDCO,MSM,EME,X,R,TECD,STOR,WPX,ASB,TCBI,TDC,DST,WSM,THG,PK,GRUB,LOPE,G,LFUS,WWD,RYN,EVHC,CUZ,DATA,CLGX,WRI,GEO,AL,OGS,CW,RBC,ALE,LPLA,MKSI,VSAT,KEX,BKH,PFPT,BKU,SNX,LSTR,SRC,SAVE,MPWR,DOC,CNO,SXT,ITT,STAY,ELLI,SHO,BOH,COR,CIEN,VSM,ISBC,HE,RDN,MBFI,ANET,LPX,OFC,TWO,PRI,LSI,CHFC,TCO,NJR,VMI,DKS,LHO,MSG,UMBF,DLX,FR,LM,GDDY,MANH,MFA,CIM,FULT,LITE,IART,TEX,CAB,CXW,IDTI,ESRT,CABO,PAYC,TXRH,HCSG,PRA,TKR,VC,DAN,RIG,HOMB,BCO,OUT,PTLA,HRB,SR,BDC,W,WTM,ERIE,CNDT,POL,INCR,NUS,CFX,PGRE,ENS,SWX,TEN,CHE,FSLR,ENTG,RAD,JBT,TGNA,NHI,FLO,SWN,BDN,SUM,PDM,CACI,PNM,PRAH,NKTR,KMT,TRCO,AA,NTCT,BRKR,STL,CACC,AHL,SF,MTZ,B,PODD,WAFD,ENR,VLY,SFM,MOH,NWE,BECN,AMCX,SPB,LEXEA,AKRX,ILG,SIGI,JACK,SLAB,FNSR,RPAI,SLCA,CATY,SANM,CLH,UNVR,WEN,TUP,BXMT,BGCP,AIZ,PDCE,CBT,EDR,DDR,VWR,ESL,HEI.A,PBI,GBCI,ZNGA,PBH,PLAY,WCC,SBH,RHP,FFIN,ESNT,MSA,ODP,CMD,CAA,USG,AAN,PBYI,CXP,BKD,AVA,WMGI,ACAD,SJI,TDS,ASGN,LPNT,EGP,DORM,DLB,NXST,KNX,IDCC,SAGE,FCFS,DY,WAGE,INT,ACIW,TSRO,GHC,FMBI,ITRI,WBT,TTEK,ZEN,FII,DAR,MUSA,KLXI,FIVE,FUL,WWW,AEIS,MTX,CBU,ICUI,VAC,VRNT,HELE,ZG,PAH,SBGI,IRWD,PSB,HA,GATX,LNCE,BXS,SKT,NUAN,NEOG,HGV,STMP,MTSI,BCPC,MDCO,TCF,HAWK,WRE,STAG,TPX,FEYE,PBF,LGF.B,HFC,AVT,QEP,SAIC,GPOR,LCII,NGVT,IBKR,JW.A,UFS,CNX,BID,SGEN,EGN,CREE,GWB,HOPE,VIAV,HQY,SSB,JUNO,KATE,BGS,RARE,FLIR,PEN,CHH,LANC,AKR,RLJ,ANAT,SMTC,CVBF,MDP,ONB,ORA,COLB,ROLL,RGC,MDRX,MOG.A,EVR,ABM,AIT,YELP,UE,LGND,CAKE,RXN,CVLT,RLI,SLGN,NYT,PFGC,GMED,GNRC,PSMT,FCNCA,DOOR,CMC,OII,CCP,ENDP,PEB,WOR,CVG,CLB,MGEE,MNK,VSH,AEL,NBR,CMP,AXE,OPK,CMPR,EFII,CLI,DRH,IRBT,GME,FNGN,DECK,KBR,SATS,UNF,SIG,HUBS,BIG,UHAL,TIVO,ATGE,BTU,UPL,SHOO,CPE,QTS,HRG,LAD,TWOU,CACQ,CHDN,AGIO,WBMD,ROIC,SAFM,SWFT,MIK,EE,ABCO,OLLI,RP,HAE,GCP,VGR,TRMK,XHR,AKS,CAR,DGI,POWI,EGBN,CLF,RNG,HI,BWLD,KBH,P,MDR,LTC,PEGA,DDD,PAY,BUFF,HL,LC,WLL,CBI,TPH,TREX,PZZA,JJSF,OMI,ROG,ICPT,IBOC,BYD,BLD,AWI,CJ,IIVI,PLNT,CBM,ACXM,HTH,FCB,ALEX,HZNP,BOKF,GVA,RH,FGEN,AEO,MRCY,BLMN,OAS,CTB,WFT,CARS,DBD,GNW,MATW,UCBI,AMN,IVR,CCMP,EAT,MWA,NSR,UNFI,PCH,NVRO,ATI,KW,DRQ,AF,RNST,SM,HYH,KWR,CSOD,MTDR,MLHR,HEI,PCRX,MMSI,ALGT,MSTR,CPS,SEMG,LXP,BANR,EXLS,KFY,SYNA,PLXS,SSD,CFFN,CWT,RDUS,HNI,SCL,WTS,GOV,MYGN,SRPT,SITE,PENN,CRS,PLT,AGII,AWR,DNOW,MGLN,SKYW,TOWN,NWN,QCP,INDB,TWLO,UFPI,PLCE,KS,FDP,PRAA,AVP,PEGI,MLI,LPI,SFBS,TMHC,WSBC,ENV,NXTM,LTXB,MXL,CCOI,APOG,UVV,FFBC,FELE,ESGR,SEM,GDOT,MORE,COLM,MTH,GRPN,NEWR,FWRD,BHE,NBTB,PRLB,RRR,TBPH,SPN,TTMI,AMED,LZB,KMPR,KRG,IOSP,AAT,HMSY,ESV,OTTR,INFN,CDE,CVA,THC,WPG,AMBA,AVXS,DF,AIN,BRC,HMN,FOE,NPO,HALO,SFLY,PFS,PAG,KN,EXPO,SIR,XOG,WDR,CATM,SBRA,FHB,TRTN,SFNC,AN,MCY,TGI,ONCE,LGF.A,ETSY,MRC,FLOW,GNL,DK,WDFC,XPER,MTOR,ESE,MDC,CORE,AAON,AZZ,ARCH,NYRT,FCN,KALU,CBL,IMPV,GEF,MEI,TWNK,APAM,BRKS,NWBI,NSIT,THRM,BOBE,PRGS,TVTY,SCSS,OIS,URBN,WABC,CHSP,ATU,SGMS,FIX,MSGN,SC,NAVG,WD,CSGS,FTR,JCP,JELD,KAMN,ITGR,BOFI,BOX,BHLB,PRK,CRZO,TNET,MORN,CTRE,TTD,OSIS,OMF,NTGR,TDOC,BSFT,RES,MINI,AAWW,TNC,RWT,CDEV,VG,IPHI,GIMO,AXL,EIG,XON,TRUE,MATX,MNRO,GPI,RDC,HSC,HMHC,NAV,TIME,VSTO,SYKE,CNMD,RMBS,CYS,HF,WERN,NSP,VREX,VECO,WNC,UBNT,EVH,AOBC,GBX,FCF,TILE,WEB,BPFH,HUBG,LQ,KCG,ACCO,RAVN,AYR,EGOV,STBA,MNTA,EBIX,IPXL,WETF,BCC,WSTC,SEB,PCTY,SAM,ATSG,MBI,PMT,CHS,SXI,KRNY,BEL,ASTE,DSW,BKFS,NVCR,SWM,TPC,CAL,TMP,ABAX,RUSHA,AIR,PSTG,CUB,FSP,TBI,AMC,CALM,NYLD,EBS,AIMC,SEAS,BMI,SCS,TERP,FIZZ,CYH,PNK,KRA,ABG,SONC,GTLS,CBF,SRG,COKE,BRKL,MANT,LHCG,FET,ENSG,RGR,AFSI,MC,CNSL,BNCL,ARR,SUPN,RPT,COTV,CENTA,SCSC,GBT,LADR,SPXC,FBP,NGHC,EVTC,STBZ,SSP,HSNI,CMO,ADTN,FNFV,GCI,FIBK,UNT,EPAY,STRA,SAFT,IPCC,SNHY,INVA,CEVA,CIR,BGG,CHCO,UHT,INN,GIII,WTW,PRIM,ARI,SMCI,PKY,COUP,HURN,HTLD,TRS,SGYP,OXM,FCH,KND,HRI,SNDR,CPLA,GSAT,KNL,SCHL,NCI,ECPG,GKOS,DYN,ACOR,CPF,REXR,IRDM,CBPX,AERI,ALOG,LNN,ADSW,TROX,ISCA,SHLM,FORM,UFCS,NE,FIT,SVU,RRD,JOE,TFSL,ANDE,DIOD,MTSC,NNI,BCOR,DDS,QSII,ALX,MTGE,EXTN,PJC,CUDA,IPHS,FWONA,GLT,WING,ABCB,ANF,CNS,MCRN,NBHC,WIRE,LMNX,QUOT,SNR,STAR,CBB,TPRE,GDI,DEL,MYCC,AMKR,MHLD,AVAV,GMS,ETH,MTCH,OFIX,LTRPA,HLX,INOV,MOD,INGN,AMBC,GFF,WMS,LSCC,BGC,AROC,BANF,CLW,CCC,TYPE,BJRI,SSTK,MTW,IRET,HIFR,DFIN,DIN,KOP,PEI,CENX,MTRN,MGRC,CRAY,GES,WIN,PHH,VRTS,HT,BLDR,NX,ACIA,QDEL,ATNI,FOLD,FOR,SHEN,ATKR,ARRY,NWLI,CZR,NFBK,LL,USNA,WWE,GPRO,DPLO,AEGN,TTS,GTY,PAHC,NTNX,QUAD,LXRX,LKSD,AVX,NEWM,BFS,RYAM,SHAK,AIMT,HSTM,ITG,DO,TREE,KELYA,ORIT,VIVO,TISI,HLI,HEES,FPO,GOGO,LCI,UIS,RPXC,AMAG,SCHN,NHC,GCO,FARO,AAXN,IPAR,HTZ,ATW,NR,THR,GLRE,LRN,EPZM,FBC,GHDX,FFG,FMSA,DNR,WAIR,RCII,DCOM,NYLD.A,UBA,RESI,KERX,BABY,LPSN,STRP,RATE,ANH,BW,SNCR,SYNT,BNFT,ELF,TTEC,ESND,LAUR,FND,BL,GNC,TLRD,CROX,CKH,SPPI,NPK,TR,EIGI,ARNA,LORL,ACHN,INWK,AHT,INSM,HY,RUN,NSM,BKE,SPWR,TMST,TELL,MITT,ARCB,EXPR,CHUBK,XLRN,ANGI,USM,FRAC,HDP,ALDR,FTK,SUP,FINL,SAH,LDR,TG,GHL,FRGI,KFRC,PRTY,NCMI,BH,VIRT,MGI,PUMP,AVD,WMK,HAYN,CRVL,MMI,EDIT,SGRY,GOLF,HIBB,REVG,BKS,OFG,GRC,ALJ,TXMD,JAG,FOSL,ACTA,IVC,KRO,CPSI,CATO,FPRX,ITCI,FRAN,ASNA,ZIOP,AFI,PDLI,VRTU,PIR,EEX,MOV,GPRE,RECN,ELGX,FORR,STFC,CVI,OB,MG,ARAY,MULE,BV,BNED,PGEM,FGL,INSY,APEI,PTCT,ADRO,RGS,PKE,PFSI,SXC,NTRA,CLNE,ICON,FRED,RMR,FF,FTD,WRLD,CENT,KODK,SMHI,HABT,INSW,ATRO,TTI,TWI,TRC,DEPO,WOW,BETR,CLDX,LOCO,ARII,RPD,CALX,OKTA,BPI,HCC,BOJA,GEF.B,NEWS,PSDO,RFP,SN,REV,RUSHB,ASPS,CHUBA,MEDP,OCN,SFS,ZUMZ,WRD,MCRB,TRK,KREF,NCSM,CLDR,CSLT,ARA,NTLA,HLIT,LE,SHLD,EZPW,SONS,CCO,AC,NVAX,AYX,FRTA,AHP,MOBL,EPE,VSI,PBPB,GBL,VSLR,SYX,YEXT,APPN,ECR,HLNE,VRA,TESO,NK,ONDK,ENT,BRS,MACK,SCWX,I,UBP,CIE";
		fileNamesAndStocks.put("D:\\Wkspace\\updateddata\\CRSP US Total Market_IndividualMarketValue.txt",CRSP_US_Total_Market);
		fileNamesAndStocks.put("D:\\Wkspace\\updateddata\\CRSP US Large Cap Value_IndividualMarketValue.txt",CRSP_US_Large_Cap_Value);
		fileNamesAndStocks.put("D:\\Wkspace\\updateddata\\CRSP US MID CAP VALUE_IndividualMarketValue.txt",CRSP_US_MID_CAP_VALUE);
		fileNamesAndStocks.put("D:\\Wkspace\\updateddata\\CRSP US SMALL CAP VALUE_IndividualMarketValue.txt",CRSP_US_SMALL_CAP_VALUE);
		
		Set<Entry<String, String>> enrties = fileNamesAndStocks.entrySet();
		for(Entry<String, String> entry:enrties){
			HttpURLConnectionExample http = new HttpURLConnectionExample(entry.getKey(),entry.getValue());
			executorService.execute(http);
		}
		
		executorService.shutdown();
//		http.sendPost();

	}
	@Override
	public void run() {
		try {
			System.out.println("Testing 1 - Send Http GET request "+Thread.currentThread());
			sendGet();
			System.out.println("Done Processing "+Thread.currentThread());
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
	}

	// HTTP GET request
	private void sendGet() throws Exception {

		String CRPTM1=stocks;
		Double d = new Double("0.0");
		File f = new File(fileName);
		BufferedReader br = new BufferedReader(new FileReader(f));
		String line=null;
		List<String> existingData = new ArrayList<>();
		while ((line=br.readLine())!=null) {
			existingData.add(line.substring(0, line.indexOf("###")));
		}
		br.close();
		BufferedWriter bw = new BufferedWriter(new FileWriter(f,true));
		try{
			for(String ticker:CRPTM1.split(",")){
				if(existingData.contains(ticker))continue;
				String url = "http://download.finance.yahoo.com/d/quotes.csv?s="+ticker+"&f=j1\"86.991B";
		
				URL obj = new URL(url);
				HttpURLConnection con = (HttpURLConnection) obj.openConnection();
		
				// optional default is GET
				con.setRequestMethod("GET");
		
				//add request header
				con.setRequestProperty("User-Agent", USER_AGENT);
		
				int responseCode = con.getResponseCode();
				System.out.println("\n Sending 'GET' request to URL : " + url + " --- "+Thread.currentThread());
				System.out.println("Response Code : " + responseCode);
		
				BufferedReader in = new BufferedReader(
				        new InputStreamReader(con.getInputStream()));
				String inputLine;
				StringBuffer response = new StringBuffer();
		
				while ((inputLine = in.readLine()) != null) {
					response.append(inputLine);
				}
				in.close();
		
				//print result
				String totalData=response.toString();
				if(totalData.contains("B")){
					String marketReturn=totalData.substring(0,totalData.indexOf("B"));
					double val = Double.parseDouble(marketReturn);
					val *= 1000;
					d+=val;
					bw.write(ticker+"###"+val+"\n");
				}else if(totalData.contains("M")){
					String marketReturn=totalData.substring(0,totalData.indexOf("M"));
					d+=Double.parseDouble(marketReturn);
					double val = Double.parseDouble(marketReturn);
					d+=val;
					bw.write(ticker+"###"+val+"\n");
				}
				else{
					double val = Double.parseDouble("0.0");
					bw.write(ticker+"###"+val+"\n");
				}
				bw.flush();
				System.out.println(d);
			}
			bw.close();
			System.out.println("Here we are "+d);
		}catch(Exception e){
			e.printStackTrace();
		}

	}

	// HTTP POST request
	private void sendPost() throws Exception {

		String url = "https://selfsolve.apple.com/wcResults.do";
		URL obj = new URL(url);
		HttpsURLConnection con = (HttpsURLConnection) obj.openConnection();

		//add reuqest header
		con.setRequestMethod("POST");
		con.setRequestProperty("User-Agent", USER_AGENT);
		con.setRequestProperty("Accept-Language", "en-US,en;q=0.5");

		String urlParameters = "sn=C02G8416DRJM&cn=&locale=&caller=&num=12345";

		// Send post request
		con.setDoOutput(true);
		DataOutputStream wr = new DataOutputStream(con.getOutputStream());
		wr.writeBytes(urlParameters);
		wr.flush();
		wr.close();

		int responseCode = con.getResponseCode();
		System.out.println("\nSending 'POST' request to URL : " + url);
		System.out.println("Post parameters : " + urlParameters);
		System.out.println("Response Code : " + responseCode);

		BufferedReader in = new BufferedReader(
		        new InputStreamReader(con.getInputStream()));
		String inputLine;
		StringBuffer response = new StringBuffer();

		while ((inputLine = in.readLine()) != null) {
			response.append(inputLine);
		}
		in.close();

		//print result
		System.out.println(response.toString());

	}

	

}