/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

//package jdbc;

import java.math.BigInteger;
import java.sql.*;
import oracle.jdbc.*;
import java.util.*;

/**
 *
 * @author sebbu
 */
public class oracle_sql {
	
	private static Connection connect;
	private static String login="sb177359";//lowercase mandatory
	private static String password="sb177359";
	private static Statement stmt;
	private static ResultSet rset;
	private static String sql;
	private static Integer num_line=0;
	
	private static ArrayList<String> tables;
	private static ArrayList<String> views;
	private static ArrayList<String> synonyms;
	private static ArrayList<String> indexes;
	private static ArrayList<String> types;
	private static ArrayList<String> procedures;
	private static ArrayList<String> sequences;
	private static ArrayList<String> triggers;
	private static TreeMap<String,String> comments1;
	private static TreeMap<String,TreeMap<String,String>> comments2;
	private static TreeMap<String,ArrayList<String>> columns;
	
	public static void print(String s) {
		System.out.print(s);
	}
	
	public static void println(String s) {
		System.out.println(s);
	}
	
	// <editor-fold defaultstate="collapsed" desc="listAll() List all objects">
	public static void listAll() {
		try {
			//liste des tables, view, synonyms
			//sql = "SELECT TNAME, TABTYPE FROM TAB ORDER BY TNAME ASC";
			//liste des tables, view, synonyms, indexes, procedures, sequences, triggers
			sql = "SELECT OBJECT_NAME, OBJECT_TYPE FROM USER_OBJECTS ORDER BY OBJECT_TYPE, OBJECT_NAME";
			Integer i=1;
			rset = stmt.executeQuery(sql);
			num_line = 0;
			while (rset.next()) {
				i=1;
				String nom = rset.getString(i);i++;
				String type = rset.getString(i);i++;
				// <editor-fold defaultstate="collapsed" desc="Type processing">
				if (type.toUpperCase().equals("TABLE")) {
					tables.add(nom);
					println("TABLE "+nom);
				}
				else if (type.toUpperCase().equals("VIEW")) {
					views.add(nom);
					println("VIEW "+nom);
				}
				else if (type.toUpperCase().equals("SYNONYM")) {
					synonyms.add(nom);
					println("SYNONYM "+nom);
				}
				else if (type.toUpperCase().equals("INDEX")) {
					indexes.add(nom);
					println("INDEX "+nom);
				}
				else if (type.toUpperCase().equals("TYPE")) {
					types.add(nom);
					println("TYPE "+nom);
				}
				else if (type.toUpperCase().equals("PROCEDURE")) {
					procedures.add(nom);
					println("PROCEDURE "+nom);
				}
				else if (type.toUpperCase().equals("SEQUENCE")) {
					sequences.add(nom);
					println("SEQUENCE "+nom);
				}
				else if (type.toUpperCase().equals("TRIGGER")) {
					triggers.add(nom);
					println("TRIGGER "+nom);
				}// </editor-fold>
				println("");
				num_line++;
			}
			if (num_line == 0) {
				//println("-- aucune table/view/synonym sur la base de données");
				println("-- aucune table/view/synonym/index/procedure/sequence/trigger sur la base de données");
				println("");
			}
			rset.close();
		} catch (SQLException ex) {
			ex.printStackTrace();
		}
	}// </editor-fold>
	
	// <editor-fold defaultstate="collapsed" desc="listTabComments() List all table comments">
	public static void listTabComments() {
		try {
			//liste des commentaires de tables
			sql = "SELECT TABLE_NAME, COMMENTS FROM USER_TAB_COMMENTS WHERE COMMENTS IS NOT NULL ORDER BY TABLE_NAME";
			Integer i=1;
			rset = stmt.executeQuery(sql);
			num_line = 0;
			while (rset.next()) {
				i=1;
				String nom = rset.getString(i);i++;
				String comment = rset.getString(i);i++;
				comments1.put(nom, comment);
				println("COMMENT ON TABLE " + nom + " IS '" + comment + "';");
				println("");
				num_line++;
			}
			if (num_line == 0) {
				println("-- aucun commentaire de table sur la base de données");
				println("");
			}
			rset.close();
		} catch (SQLException ex) {
			ex.printStackTrace();
		}

	}// </editor-fold>
	
	// <editor-fold defaultstate="collapsed" desc="listColComments() List all column comments">
	public static void listColComments() {
		try {
			//liste des commentaires de colonnes de tables
			sql = "SELECT TABLE_NAME, COLUMN_NAME, COMMENTS FROM USER_COL_COMMENTS WHERE COMMENTS IS NOT NULL ORDER BY TABLE_NAME";
			Integer i = 1;
			rset = stmt.executeQuery(sql);
			num_line = 0;
			while (rset.next()) {
				i = 1;
				String nom = rset.getString(i);i++;
				String col = rset.getString(i);	i++;
				String comment = rset.getString(i);i++;
				comments2.put(nom, new TreeMap<String, String>());
				comments2.get(nom).put(col, comment);
				println("COMMENT ON COLUMN " + nom + "." + col + " IS '" + comment + "';");
				println("");
				num_line++;
			}
			if (num_line == 0) {
				println("-- aucun commentaire de colonne de table sur la base de données");
				println("");
			}
			rset.close();
		} catch (SQLException ex) {
			ex.printStackTrace();
		}
	}// </editor-fold>
	
	// <editor-fold defaultstate="collapsed" desc="listRelTabColumns() List all relationnal table columns">
	public static void listRelTabColumns() {
		try {
			//liste des colonnes des tables relationnelles ( create table )
			/*sql="SELECT TABLE_NAME, COLUMN_NAME, DATA_TYPE, DATA_TYPE_MOD, DATA_LENGTH, DATA_PRECISION, DATA_SCALE, NULLABLE, DATA_DEFAULT\n"+
				"FROM USER_TAB_COLUMNS ORDER BY TABLE_NAME, COLUMN_ID";//*/
			sql="SELECT utc.TABLE_NAME, utc.COLUMN_NAME, DATA_TYPE, DATA_TYPE_MOD, DATA_LENGTH, DATA_PRECISION, DATA_SCALE, NULLABLE, DATA_DEFAULT,\n"+
				"IS_SCOPED, SCOPE_TABLE_OWNER, SCOPE_TABLE_NAME\n"+
				"FROM USER_TAB_COLUMNS utc JOIN USER_TABLES ut ON (utc.TABLE_NAME=ut.TABLE_NAME)\n"+
				"LEFT JOIN USER_REFS ur ON (utc.TABLE_NAME=ur.TABLE_NAME AND utc.COLUMN_NAME=ur.COLUMN_NAME)\n"+
				"ORDER BY TABLE_NAME, COLUMN_ID";
			Integer i=1;
			rset = stmt.executeQuery(sql);
			num_line = 0;
			String sql2 = "";
			String table_name1 = "";
			while (rset.next()) {
				i=1;
				String table_name2 = rset.getString(i);i++;
				if (!tables.contains(table_name2)) {
					continue;
				}
				String column_name = rset.getString(i);i++;
				String data_type = rset.getString(i);i++;
				String data_type_mod = rset.getString(i);i++;
				Integer data_length = rset.getInt(i);i++;
				Integer data_precision = rset.getInt(i);i++;
				Integer data_scale = rset.getInt(i);i++;
				String nullable2 = rset.getString(i);i++;
				Boolean nullable = nullable2.equals("Y");
				String data_default = rset.getString(i);i++;
				String scoped2 = rset.getString(i);i++;
				Boolean scoped = (scoped2 == null) ? false : scoped2.toLowerCase().equals("yes");
				String s_owner = rset.getString(i);i++;
				String s_table_name = rset.getString(i);i++;
				// <editor-fold defaultstate="collapsed" desc="new table">
				if (!table_name2.equals(table_name1)) {
					columns.put(table_name2, new ArrayList<String>());
					if (!sql2.equals("")) {
						sql2 = sql2.substring(0, sql2.length() - 2);
						sql2 += "\n);";
						println(sql2);
						println("");
						sql2 = "";
					}
					table_name1 = table_name2;
					sql2 += "CREATE TABLE " + table_name2 + " (\n";
				}// </editor-fold>
				columns.get(table_name2).add(column_name);
				sql2 += "  " + column_name + "\t";
				// <editor-fold defaultstate="collapsed" desc="type">
				if (data_type_mod != null && data_type_mod.toUpperCase().equals("REF")) {
					sql2 += "REF " + data_type;
				} else if (!data_type.toUpperCase().equals("NUMBER") && !data_type.toUpperCase().equals("DATE") && 
				!data_type.toUpperCase().equals("CLOB") && !data_type.toUpperCase().equals("BLOB")) {
					sql2 += data_type + "(" + data_length + ")";
				}
				if (data_type.toUpperCase().equals("NUMBER")) {
					if (data_precision == null && data_scale == 0) {
						sql2 += "INTEGER";
					}
					if (data_precision == null && data_scale == null) {
						sql2 += "NUMBER";
					}
					if (data_precision != null) {
						sql2 += "NUMBER(" + data_precision;
					}
					if (data_scale != null && data_scale != 0) {
						sql2 += "," + data_scale;
					}
					if (data_precision != null) {
						sql2 += ")";
					}
				}// </editor-fold>
				sql2 += " ";
				if (!nullable) {
					sql2 += "NOT ";
				}
				sql2 += "NULL ";
				if (data_default != null) {
					sql2 += "DEFAULT " + data_default;
				}
				if (scoped) {
					sql2 += "SCOPE IS ";
					if (!s_owner.equals("") && !s_owner.toLowerCase().equals(login)) {
						sql2 += s_owner + ".";
					}
					sql2 += s_table_name;
				}
				sql2 += ",\n";
				num_line++;
			}
			if (num_line == 0) {
				println("-- aucune table relationnelle sur la base de données");
				println("");
			} else {
				sql2 = sql2.substring(0, sql2.length() - 2);
				sql2 += "\n);";
				println(sql2);
				println("");
			}
			rset.close();
		} catch (SQLException ex) {
			ex.printStackTrace();
		}
	}// </editor-fold>
	
	// <editor-fold defaultstate="collapsed" desc="listObjTables() List all object table">
	public static void listObjTables() {
		try {
			//liste des colonnes des tables objets ( create table ... of )
			sql="SELECT TABLE_NAME, OBJECT_ID_TYPE, TABLE_TYPE FROM USER_OBJECT_TABLES WHERE NESTED='NO'";
			Integer i=1;
			rset = stmt.executeQuery(sql);
			num_line=0;
			String sql2="";
			while(rset.next()) {
				i=1;
				sql2="";
				String table_name2=rset.getString(i);i++;
				String object_id_type=rset.getString(i);i++;
				String data_type=rset.getString(i);i++;
				if(!object_id_type.equals("SYSTEM GENERATED")) {
					println("-- [warning] not system generated.");
				}
				sql2+="CREATE TABLE "+table_name2+" OF "+data_type+";";
				println(sql2);
				println("");
				num_line++;
			}
			if(num_line==0) {
				println("-- aucune table objet sur la base de données");
				println("");
			}
			rset.close();
		} catch (SQLException ex) {
			ex.printStackTrace();
		}
	}// </editor-fold>
	
	// <editor-fold defaultstate="collapsed" desc="listTypes() List all types">
	public static void listTypes() {
		try {
			//liste des types
			sql = "SELECT NAME, CONCAT( DECODE(SUBSTR(UPPER(TEXT),0,4),'TYPE','CREATE ',''), TEXT ) FROM USER_SOURCE WHERE TYPE='TYPE' ORDER BY NAME,LINE";
			Integer i = 1;
			rset = stmt.executeQuery(sql);
			num_line = 0;
			String sql2 = "";
			String name1 = "";
			while (rset.next()) {
				i=1;
				String name2 = rset.getString(i);i++;
				String text = rset.getString(i).replaceFirst("^(\r\n|\n\r|\n|\r)?(.+)(\r\n|\n\r|\n|\r)?$", "$2");i++;
				if (!name2.equals(name1)) {
					if (!sql2.equals("")) {
						sql2 += "/\nshow errors;";
						println(sql2);
						println("");
					}
					sql2 = "";
					name1 = name2;
				}
				sql2 += text + "\n";
				num_line++;
			}
			if (num_line == 0) {
				println("-- aucun type sur la base de données");
				println("");
			} else {
				sql2 += "/\nshow errors;";
				println(sql2);
				println("");
			}
			rset.close();
		} catch (SQLException ex) {
			ex.printStackTrace();
		}
	}// </editor-fold>
	
	// <editor-fold defaultstate="collapsed" desc="listIndexes() List all indexes">
	public static void listIndexes() {
		try {
			//liste des indexes ( sans uniques et primary key )
			/*sql = "select ui.INDEX_NAME, ui.TABLE_NAME, UNIQUENESS, "+//"INCLUDE_COLUMN, TABLE_OWNER, "+
				"uic.COLUMN_NAME, "+//"COLUMN_POSITION, COLUMN_LENGTH, CHAR_LENGTH, "+
				"DESCEND, "+//"CONSTRAINT_TYPE, "+
				"utc.DATA_DEFAULT\n"+
				"from USER_INDEXES ui\n"+
				"JOIN USER_IND_COLUMNS uic ON (ui.table_name=uic.table_name and ui.index_name=uic.index_name )\n"+
				"LEFT JOIN USER_CONSTRAINTS uc ON ( ui.index_name=uc.constraint_name AND ui.table_name=uc.table_name )\n"+
				"LEFT JOIN USER_CONS_COLUMNS ucc ON ( ui.table_name=ucc.table_name AND ui.index_name=ucc.constraint_name AND uic.column_position=ucc.position )\n"+
				"LEFT JOIN USER_TAB_COLS utc ON ( uic.TABLE_NAME=utc.TABLE_NAME AND uic.COLUMN_NAME=utc.COLUMN_NAME )\n"+
				"order by TABLE_NAME, INDEX_NAME, COLUMN_POSITION";//*/
			sql = "select ui.INDEX_NAME, ui.TABLE_NAME, UNIQUENESS, "+//"INCLUDE_COLUMN, TABLE_OWNER, "+
				"uic.COLUMN_NAME, "+//"COLUMN_POSITION, COLUMN_LENGTH, CHAR_LENGTH, "+
				"DESCEND, "+//"CONSTRAINT_TYPE, "+
				"utc.DATA_DEFAULT\n"+
				"from USER_INDEXES ui\n"+
				"JOIN USER_IND_COLUMNS uic ON (ui.table_name=uic.table_name and ui.index_name=uic.index_name )\n"+
				"LEFT JOIN USER_CONSTRAINTS uc ON ( ui.index_name=uc.constraint_name AND ui.table_name=uc.table_name )\n"+
				"LEFT JOIN USER_CONS_COLUMNS ucc ON ( ui.table_name=ucc.table_name AND ui.index_name=ucc.constraint_name AND uic.column_position=ucc.position )\n"+
				"LEFT JOIN USER_TAB_COLS utc ON ( uic.TABLE_NAME=utc.TABLE_NAME AND uic.COLUMN_NAME=utc.COLUMN_NAME )\n"+
				"WHERE uic.COLUMN_NAME!='SYS_NC_OID$' AND NOT ( REGEXP_LIKE(NVL(ucc.COLUMN_NAME,uic.COLUMN_NAME), '^SYS_NC[0-9]+\\$$') AND DATA_DEFAULT IS NULL )\n"+
				"AND CONSTRAINT_TYPE IS NULL\n"+//*/
				"order by TABLE_NAME, INDEX_NAME, COLUMN_POSITION";
			Integer i = 1;
			rset = stmt.executeQuery(sql);
			num_line = 0;
			String sql2 = "";
			String index_name1 = "";
			String descend1 = "";
			while (rset.next()) {
				i=1;
				String index_name2 = rset.getString(i);i++;
				String table_name = rset.getString(i);i++;
				if (!indexes.contains(index_name2)) {
					continue;
				}
				String uniqueness = rset.getString(i);i++;
				String column_name = rset.getString(i);i++;
				String descend2 = rset.getString(i);i++;
				String data_default = rset.getString(i);i++;
				// <editor-fold defaultstate="collapsed" desc="new index">
				if (!index_name2.equals(index_name1)) {
					if (!sql2.equals("")) {
						sql2 = sql2.substring(0, sql2.length() - 2);
						sql2 += " ) "+descend1+";";
						println(sql2);
						println("");
						sql2 = "";
					}
					index_name1 = index_name2;
					descend1 = descend2;
					sql2 += "CREATE ";
					if (uniqueness.equals("UNIQUE")) {
						sql2 += "UNIQUE ";
					}
					sql2 += "INDEX " + index_name2 + " ON " + table_name + " ( ";
				}
				// </editor-fold>
				if (data_default == null || data_default.length() == 0) {
					sql2 += column_name + ", ";
				} else {
					sql2 += data_default + ", ";
				}
				num_line++;
			}
			if (num_line == 0) {
				println("-- aucun index sur la base de données");
				println("");
			} else {
				sql2 = sql2.substring(0, sql2.length() - 2);
				sql2 += " ) "+descend1+";";
				println(sql2);
				println("");
			}
			rset.close();
		} catch (SQLException ex) {
			ex.printStackTrace();
		}
	}// </editor-fold>
	
	// <editor-fold defaultstate="collapsed" desc="listConstraints() List all constraints">
	public static void listConstraints() {
		try {
			//liste des contraintes
			sql = "select UCC.TABLE_NAME, UCC.CONSTRAINT_NAME, UCC.COLUMN_NAME, "+//"UCC.POSITION, "+
				"UC.CONSTRAINT_TYPE, UC.SEARCH_CONDITION,\n"+
				"UC2.TABLE_NAME as REFERENCES_TABLE, UCC2.COLUMN_NAME AS REFERENCES_COLUMN\n"+
				"from USER_CONS_COLUMNS UCC, USER_CONSTRAINTS UC, USER_CONSTRAINTS UC2, USER_CONS_COLUMNS UCC2\n"+
				"where UCC.CONSTRAINT_NAME = UC.CONSTRAINT_NAME\n"+
				"and UC.R_CONSTRAINT_NAME = UC2.CONSTRAINT_NAME(+)\n"+
				"and UC2.CONSTRAINT_NAME = UCC2.CONSTRAINT_NAME(+)\n"+
				"and ucc.COLUMN_NAME!='SYS_NC_OID$' AND NOT REGEXP_LIKE(ucc.COLUMN_NAME, '^SYS_NC[0-9]+\\$$')\n"+//*/
				"order by UCC.TABLE_NAME, UCC.CONSTRAINT_NAME, UCC.POSITION";
			Integer i = 1;
			rset = stmt.executeQuery(sql);
			num_line = 0;
			String sql2 = "";
			String sql3 = "";
			String constraint_name1 = "";
			while (rset.next()) {
				i = 1;
				String table_name = rset.getString(i);i++;
				String constraint_name2 = rset.getString(i);i++;
				//if(!constraint.contains(constraint_name)) continue;
				sql2 += "";
				String column_name = rset.getString(i);i++;
				//Integer position=rset.getInt(i);i++;
				String constraint_type = rset.getString(i);i++;
				String search_condition = rset.getString(i);i++;
				String reference_table = rset.getString(i);i++;
				String reference_column = rset.getString(i);i++;
				// <editor-fold defaultstate="collapsed" desc="new constraint">
				if (!constraint_name2.equals(constraint_name1)) {
					if (!sql2.equals("")) {
						sql2 = sql2.substring(0, sql2.length() - 2);
						sql2 += " )";
						sql2 += sql3;
						sql2 += ";\n\n";
						println(sql2);
						println("");
						sql2 = "";
						sql3 = "";
					}
					constraint_name1 = constraint_name2;
					sql2 += "ALTER TABLE " + table_name + " ADD CONSTRAINT ";
					sql2 += constraint_name2 + " ";
					if (constraint_type.equals("P")) {
						sql2 += "PRIMARY KEY";
					}
					if (constraint_type.equals("U")) {
						sql2 += "UNIQUE";
					}
					if (constraint_type.equals("R")) {
						sql2 += "FOREIGN KEY"; //REFERENCES
						sql3 = " REFERENCES " + reference_table + " ( " + reference_column + " )";
					}
					if (constraint_type.equals("C")) {
						sql2 += "CHECK";
					}
					if (constraint_type.equals("V")) {
						sql2 += "CHECK";
					}
					if (constraint_type.equals("O")) {
						sql2 += "READ ONLY";
					}
					if (constraint_type.equals("H")) {
						sql2 += "HASH";
					}
					if (constraint_type.equals("F")) {
						sql2 += "";
					}
					if (constraint_type.equals("S")) {
						sql2 += "";
					}
					sql2 += " ( ";
				}
				// </editor-fold>
				sql2 += column_name;
				if (constraint_type.equals("C") || constraint_type.equals("V")) {
					sql2 += " " + search_condition;
				}
				sql2 += ", ";
				num_line++;
			}
			if (num_line == 0) {
				println("-- aucune contrainte sur la base de données");
				println("");
			} else {
				sql2 = sql2.substring(0, sql2.length() - 2);
				sql2 += " );";
				println(sql2);
				println("");
			}
			rset.close();
		} catch (SQLException ex) {
			ex.printStackTrace();
		}
	}// </editor-fold>
	
	// <editor-fold defaultstate="collapsed" desc="listViews() List all views">
	public static void listViews() {
		try {
			//liste des vues
			sql = "select UV.VIEW_NAME, UV.TEXT\n"+//", UTC.COMMENTS\n"+
				"from USER_VIEWS UV\n"+//", USER_TAB_COMMENTS UTC\n"+
				//"where UV.VIEW_NAME = UTC.TABLE_NAME(+)\n"+
				"order by uv.view_name";
			Integer i=1;
			rset = stmt.executeQuery(sql);
			num_line = 0;
			while (rset.next()) {
				i=1;
				String view_name = rset.getString(i);i++;
				String text = rset.getString(i);i++;
				//String comment = rset.getString(i);i++;
				println("CREATE OR REPLACE VIEW \"" + view_name + "\" AS\n" + text + ";");
				println("");
				//println("COMMENT ON TABLE \""+ view_name +"\" IS \'"+comment+"\';");
				//println("");
				num_line++;
			}
			if (num_line == 0) {
				println("-- aucune vue sur la base de données");
				println("");
			}
			rset.close();
		} catch (SQLException ex) {
			ex.printStackTrace();
		}
	}// </editor-fold>
	
	// <editor-fold defaultstate="collapsed" desc="listSequences() List all sequences">
	public static void listSequences() {
		try {
			//liste des sequences
			sql = "select SEQUENCE_NAME, MIN_VALUE, MAX_VALUE, INCREMENT_BY, CYCLE_FLAG, ORDER_FLAG, CACHE_SIZE, LAST_NUMBER\n"+
				"from USER_SEQUENCES\n"+
				"order by sequence_name";
			Integer i = 1;
			rset = stmt.executeQuery(sql);
			num_line = 0;
			while (rset.next()) {
				i = 1;
				String seq_name = rset.getString(i);i++;
				//String minvalue=rset.getString(i);i++;
				BigInteger minvalue = rset.getBigDecimal(i).toBigInteger();i++;
				//String maxvalue=rset.getString(i);
				BigInteger maxvalue = rset.getBigDecimal(i).toBigInteger();i++;
				//String incr=rset.getString(i);i++;
				BigInteger incr = rset.getBigDecimal(i).toBigInteger();i++;
				String cycle = rset.getString(i);i++;
				String order = rset.getString(i);i++;
				//String cache=rset.getString(i);i++;
				BigInteger cache = rset.getBigDecimal(i).toBigInteger();i++;
				String last = rset.getString(i);i++;
				String seq = "CREATE SEQUENCE \"" + seq_name + "\" MINVALUE " + minvalue + " MAXVALUE " + maxvalue + " INCREMENT BY " + incr + " START WITH " + last;
				if (cycle.equals("Y")) {
					seq += " CYCLE";
				}
				if (cycle.equals("N")) {
					seq += " NOCYCLE";
				}
				if (order.equals("Y")) {
					seq += " ORDER";
				}
				if (order.equals("N")) {
					seq += " NOORDER";
				}
				seq += " CACHE " + cache + ";";
				println(seq);
				println("");
				num_line++;
			}
			if (num_line == 0) {
				println("-- aucune sequence sur la base de données");
				println("");
			}
			rset.close();
		} catch (SQLException ex) {
			ex.printStackTrace();
		}
	}// </editor-fold>
	
	// <editor-fold defaultstate="collapsed" desc="listDbLinks() List all db links">
	public static void listDbLinks() {
		try {
			//liste db_links
			sql="select OWNER, USERNAME, HOST, regexp_replace(DB_LINK,'.REGRESS.RDBMS.DEV.US.ORACLE.COM$') as DB_LINK from ALL_DB_LINKS order by DB_LINK";
			Integer i=1;
			rset = stmt.executeQuery(sql);
			num_line = 0;
			while (rset.next()) {
				i=1;
				String owner = rset.getString(i);i++;
				String username = rset.getString(i);i++;
				String host = rset.getString(i);i++;
				String name = rset.getString(i);i++;
				sql = "CREATE ";
				if (owner != null && owner.equals("PUBLIC")) {
					sql += "PUBLIC ";
				}
				sql += "DATABASE LINK " + name + " ";
				if (username != null && username.length() > 0) {
					sql += "CONNECT TO " + username + " ";
				}
				//sql+="IDENTIFIED BY &pwd ";//si mot de passe requis
				sql += "USING " + host + ";";
				println(sql);
				println("");
				num_line++;
			}
			if (num_line == 0) {
				println("-- aucun db link sur la base de données");
				println("");
			}
			rset.close();
		} catch (SQLException ex) {
			ex.printStackTrace();
		}
	}
	// </editor-fold>
	
	// <editor-fold defaultstate="collapsed" desc="listSynonyms() List all synonyms">
	public static void listSynonyms() {
		try {
			//liste des synonymes
			sql="select SYNONYM_NAME, TABLE_OWNER, TABLE_NAME, regexp_replace(DB_LINK,'.REGRESS.RDBMS.DEV.US.ORACLE.COM$','',1,0,'i') as DB_LINK\n"+
				"from USER_SYNONYMS order by SYNONYM_NAME";
			Integer i=1;
			rset = stmt.executeQuery(sql);
			num_line = 0;
			String sql2="";
			while (rset.next()) {
				i=1;
				String name = rset.getString(i);i++;
				String owner_name = rset.getString(i);i++;
				String table_name = rset.getString(i);i++;
				String db_link = rset.getString(i);i++;
				sql2 = "CREATE SYNONYM " + name + " FOR ";
				if (owner_name != null && owner_name.length() > 0) {
					sql2 += owner_name + ".";
				}
				sql2 += table_name + "@" + db_link + ";";
				println(sql2);
				println("");
				num_line++;
			}
			if (num_line == 0) {
				println("-- aucun synonyme sur la base de données");
				println("");
			}
			rset.close();
		} catch (SQLException ex) {
			ex.printStackTrace();
		}
	}
	// </editor-fold>
	
	// <editor-fold defaultstate="collapsed" desc="listTriggers() List all triggers">
	public static void listTriggers() {
		try {
			//liste des triggers
			sql = "select TRIGGER_NAME, TRIGGER_TYPE, TRIGGERING_EVENT, TABLE_NAME, REFERENCING_NAMES, WHEN_CLAUSE, STATUS, DESCRIPTION, ACTION_TYPE,\n"+
				"TRIGGER_BODY from USER_TRIGGERS order by TRIGGER_NAME";
			Integer i = 1;
			rset = stmt.executeQuery(sql);
			num_line = 0;
			String sql2 = "";
			while (rset.next()) {
				i=1;
				//TRIGGER_NAME	TRIGGER_TYPE	TRIGGERING_EVENT	TABLE_NAME	REFERENCING_NAMES	WHEN_CLAUSE	STATUS	DESCRIPTION	ACTION_TYPE	TRIGGER_BODY
				String name = rset.getString(i);i++;
				String type=rset.getString(i);i++;
				String event=rset.getString(i);i++;
				String table_name=rset.getString(i);i++;
				String referencing = rset.getString(i);i++;
				String when = rset.getString(i);i++;
				String status = rset.getString(i);i++;
				String desc = rset.getString(i);i++;
				String action_type = rset.getString(i);i++;
				String body = rset.getString(i);i++;
				//String sql2="CREATE OR REPLACE TRIGGER "+name+" "+type+" "+event+" ON "+table_name+" FOR EACH ROW\n";
				sql2 = "CREATE OR REPLACE TRIGGER " + desc.trim() + "\n";
				if ( type != null && type.length() > 0 ) {
					sql2 += type + " ";
				}
				if ( event != null && event.length() > 0 ) {
					sql2 += event + " ";
				}
				if ( table_name != null && table_name.length() > 0 ) {
					sql2 += table_name;
				}
				if (referencing != null && referencing.length() > 0 && !referencing.equals("REFERENCING NEW AS NEW OLD AS OLD")
				&& !referencing.equals("REFERENCING NEW AS NEW")&& !referencing.equals("REFERENCING OLD AS OLD") ) {
					sql2 += referencing + "\n";
				}
				if (when != null && when.length() > 0) {
					sql2 += " WHEN (" + when + ")\n";
				}
				if ( action_type==null || action_type.length()==0 || !action_type.trim().equals("PL/SQL") ) {
					println("-- [WARNING] not PL/SQL");
				}
				sql2 += body + "\n/\nshow errors;";
				//if(status.equals("DISABLED"))
				sql2 += "\nALTER TRIGGER " + name + " " + status.substring(0, status.length() - 1) + ";";
				println(sql2);
				println("");
				num_line++;
			}
			if (num_line == 0) {
				println("-- aucun trigger sur la base de données");
				println("");
			}
			rset.close();
		} catch (SQLException ex) {
			ex.printStackTrace();
		}
	}
	// </editor-fold>
	
	// <editor-fold defaultstate="collapsed" desc="ListProcedures() List all procedures">
	public static void listProcedures() {
		try {
			//liste des PROCEDURE, PACKAGE, PACKAGE BODY, TRIGGER, FUNCTION, TYPE
			/*sql="select NAME, TYPE, "+//"LINE, "+
				"TEXT from USER_SOURCE order by NAME, TYPE, LINE";//*/
			//liste des PROCEDURE, PACKAGE, PACKAGE BODY, FUNCTION
			sql="select NAME, TYPE, "+//"LINE, "+
				"TEXT from USER_SOURCE where TYPE NOT IN ('TRIGGER','TYPE') order by NAME, TYPE, LINE";
			Integer i=1;
			rset = stmt.executeQuery(sql);
			num_line = 0;
			String name1 = "";
			String sql2 = "";
			String type2 = "";
			while (rset.next()) {
				i=1;
				String name2 = rset.getString(i);i++;
				String type = rset.getString(i);i++;
				type2 = type;
				//Integer line = rset.getInt(i);i++;
				String text = rset.getString(i);i++;
				if (!name2.equals(name1)) {
					if (!sql2.equals("")) {
						//if(type2.equals("PROCEDURE")||type2.equals("FUNCTION")||type2.equals("TYPE")||type2.equals("TYPE BODY")
						//		||type2.equals("PACKAGE")||type2.equals("PACKAGE BODY")||type2.equals("TRIGGER"))
						sql2 += "/\nshow errors;";
						println(sql2);
						println("");
						sql2 = "";
					}
					sql2 = "CREATE OR REPLACE ";
					name1 = name2;
				}
				sql2 += text.replaceFirst("^(\r\n|\n\r|\n|\r)?(.+)(\r\n|\n\r|\n|\r)?$", "$2") + "\n";
				num_line++;
			}
			if (num_line == 0) {
				System.out.println("-- aucune procedure, package ou function sur la base de données");
				//System.out.println("-- aucune procedure, package, function, trigger ou type sur la base de données");
			} else {
				sql2 += "/\nshow errors;";
				println(sql2);
			}
			rset.close();
		} catch (SQLException ex) {
			ex.printStackTrace();
		}
	}
	// </editor-fold>
	
	// <editor-fold default@state="collapsed" desc="listTypes() List types">
	// </editor-fold>
	//sql
	
	// <editor-fold desc="init() Initialize variables">
	public static void init() {
		tables=new ArrayList<String>();
		views=new ArrayList<String>();
		synonyms=new ArrayList<String>();
		indexes=new ArrayList<String>();
		types=new ArrayList<String>();
		procedures=new ArrayList<String>();
		sequences=new ArrayList<String>();
		triggers=new ArrayList<String>();
		
		comments1=new TreeMap<String,String>();
		comments2=new TreeMap<String,TreeMap<String,String>>();
		columns=new TreeMap<String,ArrayList<String>>();
	}// </editor-fold>
	
	public static void main(String[] args) {
		try {
			//decide what to do
			// <editor-fold defaultstate="collapsed" desc="Processing arguments">
			if(args.length==0) {
				println("Usage : "+getCurClassName()+" [-h|--help] [-v|--version]");
				return;
			}
			if( args.length==1 && (args[0].equals("-h")||args[0].equals("--help")) ) {
				println("Usage : "+getCurClassName()+" [options]");
				println("Options :");
				println("\t[-h|--help]\tfor help");
				println("\t[-v|--version]\tfor version information");
				return;
			}
			if( args.length==1 && (args[0].equals("-v")||args[0].equals("--version")) ) {
				println(getCurClassName()+" version 0.05 by sebbu");
			}
			// </editor-fold>
			//init
			// <editor-fold defaultstate="collapsed" desc="initializing connection">
			//Class.forName("oracle.jdbc.driver.OracleDriver");
			//DriverManager.registerDriver(new oracle.jdbc.OracleDriver());
			DriverManager.registerDriver(new OracleDriver());
			//connect=DriverManager.getConnection("jdbc:oracle:thin:@pagnol:1521:ens082", login, password);
			//connect=DriverManager.getConnection("jdbc:oracle:thin:@localhost:1521:ens082", login, password);//pagnol via ufr
			//connect=DriverManager.getConnection("jdbc:oracle:thin:@localhost:1522:ens0904", "bc", "bc");//MI105-04 via ufr
			//connect=DriverManager.getConnection("jdbc:oracle:thin:@localhost:1523:ens0917", "bc", "bc");//MI105-17 via ufr
			//connect=DriverManager.getConnection("jdbc:oracle:thin:@localhost:1522:ens091", login, password);//stendhal via ufr
			connect=DriverManager.getConnection("jdbc:oracle:thin:@localhost:1523:ens092", login, password);//butor via ufr
			stmt=connect.createStatement();
			// </editor-fold>
			//process operations
			// <editor-fold defaultstate="collapsed" desc="processing exporting structure">
			println("");
			init();println("");
			println("-- List all objects");
			listAll();println("");
			println("-- List table comments");
			listTabComments();println("");
			println("-- List column comments");
			listColComments();println("");
			println("-- List relationnal table columns");
			listRelTabColumns();println("");
			println("-- List object tables");
			listObjTables();println("");
			println("-- List types");
			listTypes();println("");
			println("-- List indexes");
			listIndexes();println("");
			println("-- List constraints");
			listConstraints();println("");
			println("-- List views");
			listViews();println("");
			println("-- List sequences");
			listSequences();println("");
			println("-- List db links");
			listDbLinks();println("");
			println("-- List synonyms");
			listSynonyms();println("");
			println("-- List triggers");
			listTriggers();println("");
			println("-- List procedures");
			listProcedures();println("");
			// </editor-fold>
			//end
			//rset.close();//shouldn't be needed
			stmt.close();
			connect.close();
		}
		catch(SQLException e) {
			e.printStackTrace();
		}
		/*catch (ClassNotFoundException e) {
			e.printStackTrace();
		}//needed by Class.forName*/
	}
	
	// <editor-fold defaultstate="collapsed" desc="GetCurClassName snipplet">
	private static String ClassName="";
	
	public static String getCurClassName(){
		if(ClassName.equals("")) ClassName=new CurClassNameGetter().getClassName();
		return ClassName;
	}

	//Static Nested Class doing the trick
	private static class CurClassNameGetter extends SecurityManager{
		public String getClassName(){
			return getClassContext()[1].getName();
		}
	}
	// </editor-fold>
	
}
