package com.h119.transcript.util;

import java.util.List;
import java.util.Objects;
import java.util.Optional;

public class LanguageCodes {

	private static final List<Language> languageCodes;

	public static Optional<Language> ofAlpha3(String alpha3) {
		return
			languageCodes
				.stream()
				.filter(
					lc -> alpha3.equals(lc.getAlpha3())
				)
				.findAny();
	}
	
	private LanguageCodes() {}

	public static class Language {
		private String name;

		/**
		 * The ISO 639-2 language code.
		 */
		private String alpha3;

		/**
		 * The ISO 639-1 language code.
		 */
		private String alpha2;

		public Language(String name, String alpha3, String alpha2) {
			this.name = name;
			this.alpha3 = alpha3;
			this.alpha2 = alpha2;
		}

		public String getName() {
			return name;
		}

		public String getAlpha3() {
			return alpha3;
		}

		public String getAlpha2() {
			return alpha2;
		}

		@Override
		public boolean equals(Object o) {
			if (o == this) return true;
			if (!(o instanceof Language)) {
				return false;
			}
			Language other = (Language) o;
			return 
				name.equals(other.name) &&
				alpha3.equals(other.alpha3) &&
				alpha2.equals(other.alpha2);
		}

		@Override
		public int hashCode() {
			return Objects.hash(name, alpha3, alpha2);
		}

		@Override
		public String toString() {
			return name;
		}
	}

	static {
		languageCodes = List.of (
			new Language("English Name of Language","ISO 639-2","ISO 639-1"),
			new Language("Abkhazian","abk","ab"),
			new Language("Achinese","ace","<na>"),
			new Language("Acoli","ach","<na>"),
			new Language("Adangme","ada","<na>"),
			new Language("Adygei","ady","<na>"),
			new Language("Adyghe","ady","<na>"),
			new Language("Afar","aar","aa"),
			new Language("Afrihili","afh","<na>"),
			new Language("Afrikaans","afr","af"),
			new Language("Afro-Asiatic languages","afa","<na>"),
			new Language("Ainu","ain","<na>"),
			new Language("Akan","aka","ak"),
			new Language("Akkadian","akk","<na>"),
			new Language("Albanian","alb/sqi","sq"),
			new Language("Alemannic","gsw","<na>"),
			new Language("Aleut","ale","<na>"),
			new Language("Algonquian languages","alg","<na>"),
			new Language("Alsatian","gsw","<na>"),
			new Language("Altaic languages","tut","<na>"),
			new Language("Amharic","amh","am"),
			new Language("Angika","anp","<na>"),
			new Language("Apache languages","apa","<na>"),
			new Language("Arabic","ara","ar"),
			new Language("Aragonese","arg","an"),
			new Language("Arapaho","arp","<na>"),
			new Language("Arawak","arw","<na>"),
			new Language("Armenian","arm/hye","hy"),
			new Language("Aromanian","rup","<na>"),
			new Language("Artificial languages","art","<na>"),
			new Language("Arumanian","rup","<na>"),
			new Language("Assamese","asm","as"),
			new Language("Asturian","ast","<na>"),
			new Language("Asturleonese","ast","<na>"),
			new Language("Athapascan languages","ath","<na>"),
			new Language("Australian languages","aus","<na>"),
			new Language("Austronesian languages","map","<na>"),
			new Language("Avaric","ava","av"),
			new Language("Avestan","ave","ae"),
			new Language("Awadhi","awa","<na>"),
			new Language("Aymara","aym","ay"),
			new Language("Azerbaijani","aze","az"),
			new Language("Bable","ast","<na>"),
			new Language("Balinese","ban","<na>"),
			new Language("Baltic languages","bat","<na>"),
			new Language("Baluchi","bal","<na>"),
			new Language("Bambara","bam","bm"),
			new Language("Bamileke languages","bai","<na>"),
			new Language("Banda languages","bad","<na>"),
			new Language("Bantu languages","bnt","<na>"),
			new Language("Basa","bas","<na>"),
			new Language("Bashkir","bak","ba"),
			new Language("Basque","baq/eus","eu"),
			new Language("Batak languages","btk","<na>"),
			new Language("Bedawiyet","bej","<na>"),
			new Language("Beja","bej","<na>"),
			new Language("Belarusian","bel","be"),
			new Language("Bemba","bem","<na>"),
			new Language("Bengali","ben","bn"),
			new Language("Berber languages","ber","<na>"),
			new Language("Bhojpuri","bho","<na>"),
			new Language("Bihari languages","bih","bh"),
			new Language("Bikol","bik","<na>"),
			new Language("Bilin","byn","<na>"),
			new Language("Bini","bin","<na>"),
			new Language("Bislama","bis","bi"),
			new Language("Blin","byn","<na>"),
			new Language("Bliss","zbl","<na>"),
			new Language("Blissymbolics","zbl","<na>"),
			new Language("Blissymbols","zbl","<na>"),
			new Language("Bokmål, Norwegian","nob","nb"),
			new Language("Bosnian","bos","bs"),
			new Language("Braj","bra","<na>"),
			new Language("Breton","bre","br"),
			new Language("Buginese","bug","<na>"),
			new Language("Bulgarian","bul","bg"),
			new Language("Buriat","bua","<na>"),
			new Language("Burmese","bur/mya","my"),
			new Language("Caddo","cad","<na>"),
			new Language("Castilian","spa","es"),
			new Language("Catalan","cat","ca"),
			new Language("Caucasian languages","cau","<na>"),
			new Language("Cebuano","ceb","<na>"),
			new Language("Celtic languages","cel","<na>"),
			new Language("Central American Indian languages","cai","<na>"),
			new Language("Central Khmer","khm","km"),
			new Language("Chagatai","chg","<na>"),
			new Language("Chamic languages","cmc","<na>"),
			new Language("Chamorro","cha","ch"),
			new Language("Chechen","che","ce"),
			new Language("Cherokee","chr","<na>"),
			new Language("Chewa","nya","ny"),
			new Language("Cheyenne","chy","<na>"),
			new Language("Chibcha","chb","<na>"),
			new Language("Chichewa","nya","ny"),
			new Language("Chinese","chi/zho","zh"),
			new Language("Chinook jargon","chn","<na>"),
			new Language("Chipewyan","chp","<na>"),
			new Language("Choctaw","cho","<na>"),
			new Language("Chuang","zha","za"),
			new Language("Church Slavic","chu","cu"),
			new Language("Church Slavonic","chu","cu"),
			new Language("Chuukese","chk","<na>"),
			new Language("Chuvash","chv","cv"),
			new Language("Classical Nepal Bhasa","nwc","<na>"),
			new Language("Classical Newari","nwc","<na>"),
			new Language("Classical Syriac","syc","<na>"),
			new Language("Cook Islands Maori","rar","<na>"),
			new Language("Coptic","cop","<na>"),
			new Language("Cornish","cor","kw"),
			new Language("Corsican","cos","co"),
			new Language("Cree","cre","cr"),
			new Language("Creek","mus","<na>"),
			new Language("Creoles and pidgins","crp","<na>"),
			new Language("Creoles and pidgins, English based","cpe","<na>"),
			new Language("Creoles and pidgins, French-based","cpf","<na>"),
			new Language("Creoles and pidgins, Portuguese-based","cpp","<na>"),
			new Language("Crimean Tatar","crh","<na>"),
			new Language("Crimean Turkish","crh","<na>"),
			new Language("Croatian","hrv","hr"),
			new Language("Cushitic languages","cus","<na>"),
			new Language("Czech","cze/ces","cs"),
			new Language("Dakota","dak","<na>"),
			new Language("Danish","dan","da"),
			new Language("Dargwa","dar","<na>"),
			new Language("Delaware","del","<na>"),
			new Language("Dene Suline","chp","<na>"),
			new Language("Dhivehi","div","dv"),
			new Language("Dimili","zza","<na>"),
			new Language("Dimli","zza","<na>"),
			new Language("Dinka","din","<na>"),
			new Language("Divehi","div","dv"),
			new Language("Dogri","doi","<na>"),
			new Language("Dogrib","dgr","<na>"),
			new Language("Dravidian languages","dra","<na>"),
			new Language("Duala","dua","<na>"),
			new Language("Dutch","dut/nld","nl"),
			new Language("Dutch, Middle (ca.1050-1350)","dum","<na>"),
			new Language("Dyula","dyu","<na>"),
			new Language("Dzongkha","dzo","dz"),
			new Language("Eastern Frisian","frs","<na>"),
			new Language("Edo","bin","<na>"),
			new Language("Efik","efi","<na>"),
			new Language("Egyptian (Ancient)","egy","<na>"),
			new Language("Ekajuk","eka","<na>"),
			new Language("Elamite","elx","<na>"),
			new Language("English","eng","en"),
			new Language("English, Middle (1100-1500)","enm","<na>"),
			new Language("English, Old (ca.450-1100)","ang","<na>"),
			new Language("Erzya","myv","<na>"),
			new Language("Esperanto","epo","eo"),
			new Language("Estonian","est","et"),
			new Language("Ewe","ewe","ee"),
			new Language("Ewondo","ewo","<na>"),
			new Language("Fang","fan","<na>"),
			new Language("Fanti","fat","<na>"),
			new Language("Faroese","fao","fo"),
			new Language("Fijian","fij","fj"),
			new Language("Filipino","fil","<na>"),
			new Language("Finnish","fin","fi"),
			new Language("Finno-Ugrian languages","fiu","<na>"),
			new Language("Flemish","dut/nld","nl"),
			new Language("Fon","fon","<na>"),
			new Language("French","fre","fr"),
			new Language("French","fra","fr"),
			new Language("French, Middle (ca.1400-1600)","frm","<na>"),
			new Language("French, Old (842-ca.1400)","fro","<na>"),
			new Language("Friulian","fur","<na>"),
			new Language("Fulah","ful","ff"),
			new Language("Ga","gaa","<na>"),
			new Language("Gaelic","gla","gd"),
			new Language("Galibi Carib","car","<na>"),
			new Language("Galician","glg","gl"),
			new Language("Ganda","lug","lg"),
			new Language("Gayo","gay","<na>"),
			new Language("Gbaya","gba","<na>"),
			new Language("Geez","gez","<na>"),
			new Language("Georgian","geo","ka"),
			new Language("Georgian","kat","ka"),
			new Language("German","ger","de"),
			new Language("German","deu","de"),
			new Language("German, Low","nds","<na>"),
			new Language("German, Middle High (ca.1050-1500)","gmh","<na>"),
			new Language("German, Old High (ca.750-1050)","goh","<na>"),
			new Language("Germanic languages","gem","<na>"),
			new Language("Gikuyu","kik","ki"),
			new Language("Gilbertese","gil","<na>"),
			new Language("Gondi","gon","<na>"),
			new Language("Gorontalo","gor","<na>"),
			new Language("Gothic","got","<na>"),
			new Language("Grebo","grb","<na>"),
			new Language("Greek, Ancient (to 1453)","grc","<na>"),
			new Language("Greek, Modern (1453-)","gre","el"),
			new Language("Greek, Modern (1453-)","ell","el"),
			new Language("Greenlandic","kal","kl"),
			new Language("Guarani","grn","gn"),
			new Language("Gujarati","guj","gu"),
			new Language("Gwich'in","gwi","<na>"),
			new Language("Haida","hai","<na>"),
			new Language("Haitian","hat","ht"),
			new Language("Haitian Creole","hat","ht"),
			new Language("Hausa","hau","ha"),
			new Language("Hawaiian","haw","<na>"),
			new Language("Hebrew","heb","he"),
			new Language("Herero","her","hz"),
			new Language("Hiligaynon","hil","<na>"),
			new Language("Himachali languages","him","<na>"),
			new Language("Hindi","hin","hi"),
			new Language("Hiri Motu","hmo","ho"),
			new Language("Hittite","hit","<na>"),
			new Language("Hmong","hmn","<na>"),
			new Language("Hungarian","hun","hu"),
			new Language("Hupa","hup","<na>"),
			new Language("Iban","iba","<na>"),
			new Language("Icelandic","ice","is"),
			new Language("Icelandic","isl","is"),
			new Language("Ido","ido","io"),
			new Language("Igbo","ibo","ig"),
			new Language("Ijo languages","ijo","<na>"),
			new Language("Iloko","ilo","<na>"),
			new Language("Imperial Aramaic (700-300 BCE)","arc","<na>"),
			new Language("Inari Sami","smn","<na>"),
			new Language("Indic languages","inc","<na>"),
			new Language("Indo-European languages","ine","<na>"),
			new Language("Indonesian","ind","id"),
			new Language("Ingush","inh","<na>"),
			new Language("Interlingua (International Auxiliary Language Association)","ina","ia"),
			new Language("Interlingue","ile","ie"),
			new Language("Inuktitut","iku","iu"),
			new Language("Inupiaq","ipk","ik"),
			new Language("Iranian languages","ira","<na>"),
			new Language("Irish","gle","ga"),
			new Language("Irish, Middle (900-1200)","mga","<na>"),
			new Language("Irish, Old (to 900)","sga","<na>"),
			new Language("Iroquoian languages","iro","<na>"),
			new Language("Italian","ita","it"),
			new Language("Japanese","jpn","ja"),
			new Language("Javanese","jav","jv"),
			new Language("Jingpho","kac","<na>"),
			new Language("Judeo-Arabic","jrb","<na>"),
			new Language("Judeo-Persian","jpr","<na>"),
			new Language("Kabardian","kbd","<na>"),
			new Language("Kabyle","kab","<na>"),
			new Language("Kachin","kac","<na>"),
			new Language("Kalaallisut","kal","kl"),
			new Language("Kalmyk","xal","<na>"),
			new Language("Kamba","kam","<na>"),
			new Language("Kannada","kan","kn"),
			new Language("Kanuri","kau","kr"),
			new Language("Kapampangan","pam","<na>"),
			new Language("Kara-Kalpak","kaa","<na>"),
			new Language("Karachay-Balkar","krc","<na>"),
			new Language("Karelian","krl","<na>"),
			new Language("Karen languages","kar","<na>"),
			new Language("Kashmiri","kas","ks"),
			new Language("Kashubian","csb","<na>"),
			new Language("Kawi","kaw","<na>"),
			new Language("Kazakh","kaz","kk"),
			new Language("Khasi","kha","<na>"),
			new Language("Khoisan languages","khi","<na>"),
			new Language("Khotanese","kho","<na>"),
			new Language("Kikuyu","kik","ki"),
			new Language("Kimbundu","kmb","<na>"),
			new Language("Kinyarwanda","kin","rw"),
			new Language("Kirdki","zza","<na>"),
			new Language("Kirghiz","kir","ky"),
			new Language("Kirmanjki","zza","<na>"),
			new Language("Klingon","tlh","<na>"),
			new Language("Komi","kom","kv"),
			new Language("Kongo","kon","kg"),
			new Language("Konkani","kok","<na>"),
			new Language("Korean","kor","ko"),
			new Language("Kosraean","kos","<na>"),
			new Language("Kpelle","kpe","<na>"),
			new Language("Kru languages","kro","<na>"),
			new Language("Kuanyama","kua","kj"),
			new Language("Kumyk","kum","<na>"),
			new Language("Kurdish","kur","ku"),
			new Language("Kurukh","kru","<na>"),
			new Language("Kutenai","kut","<na>"),
			new Language("Kwanyama","kua","kj"),
			new Language("Kyrgyz","kir","ky"),
			new Language("Ladino","lad","<na>"),
			new Language("Lahnda","lah","<na>"),
			new Language("Lamba","lam","<na>"),
			new Language("Land Dayak languages","day","<na>"),
			new Language("Lao","lao","lo"),
			new Language("Latin","lat","la"),
			new Language("Latvian","lav","lv"),
			new Language("Leonese","ast","<na>"),
			new Language("Letzeburgesch","ltz","lb"),
			new Language("Lezghian","lez","<na>"),
			new Language("Limburgan","lim","li"),
			new Language("Limburger","lim","li"),
			new Language("Limburgish","lim","li"),
			new Language("Lingala","lin","ln"),
			new Language("Lithuanian","lit","lt"),
			new Language("Lojban","jbo","<na>"),
			new Language("Low German","nds","<na>"),
			new Language("Low Saxon","nds","<na>"),
			new Language("Lower Sorbian","dsb","<na>"),
			new Language("Lozi","loz","<na>"),
			new Language("Luba-Katanga","lub","lu"),
			new Language("Luba-Lulua","lua","<na>"),
			new Language("Luiseno","lui","<na>"),
			new Language("Lule Sami","smj","<na>"),
			new Language("Lunda","lun","<na>"),
			new Language("Luo (Kenya and Tanzania)","luo","<na>"),
			new Language("Lushai","lus","<na>"),
			new Language("Luxembourgish","ltz","lb"),
			new Language("Macedo-Romanian","rup","<na>"),
			new Language("Macedonian","mac","mk"),
			new Language("Macedonian","mkd","mk"),
			new Language("Madurese","mad","<na>"),
			new Language("Magahi","mag","<na>"),
			new Language("Maithili","mai","<na>"),
			new Language("Makasar","mak","<na>"),
			new Language("Malagasy","mlg","mg"),
			new Language("Malay","may","ms"),
			new Language("Malay","msa","ms"),
			new Language("Malayalam","mal","ml"),
			new Language("Maldivian","div","dv"),
			new Language("Maltese","mlt","mt"),
			new Language("Manchu","mnc","<na>"),
			new Language("Mandar","mdr","<na>"),
			new Language("Mandingo","man","<na>"),
			new Language("Manipuri","mni","<na>"),
			new Language("Manobo languages","mno","<na>"),
			new Language("Manx","glv","gv"),
			new Language("Maori","mao","mi"),
			new Language("Maori","mri","mi"),
			new Language("Mapuche","arn","<na>"),
			new Language("Mapudungun","arn","<na>"),
			new Language("Marathi","mar","mr"),
			new Language("Mari","chm","<na>"),
			new Language("Marshallese","mah","mh"),
			new Language("Marwari","mwr","<na>"),
			new Language("Masai","mas","<na>"),
			new Language("Mayan languages","myn","<na>"),
			new Language("Mende","men","<na>"),
			new Language("Mi'kmaq","mic","<na>"),
			new Language("Micmac","mic","<na>"),
			new Language("Minangkabau","min","<na>"),
			new Language("Mirandese","mwl","<na>"),
			new Language("Mohawk","moh","<na>"),
			new Language("Moksha","mdf","<na>"),
			new Language("Moldavian","rum","ro"),
			new Language("Moldavian","ron","ro"),
			new Language("Moldovan","rum","ro"),
			new Language("Moldovan","ron","ro"),
			new Language("Mon-Khmer languages","mkh","<na>"),
			new Language("Mong","hmn","<na>"),
			new Language("Mongo","lol","<na>"),
			new Language("Mongolian","mon","mn"),
			new Language("Montenegrin","cnr","<na>"),
			new Language("Mossi","mos","<na>"),
			new Language("Multiple languages","mul","<na>"),
			new Language("Munda languages","mun","<na>"),
			new Language("N'Ko","nqo","<na>"),
			new Language("Nahuatl languages","nah","<na>"),
			new Language("Nauru","nau","na"),
			new Language("Navaho","nav","nv"),
			new Language("Navajo","nav","nv"),
			new Language("Ndebele, North","nde","nd"),
			new Language("Ndebele, South","nbl","nr"),
			new Language("Ndonga","ndo","ng"),
			new Language("Neapolitan","nap","<na>"),
			new Language("Nepal Bhasa","new","<na>"),
			new Language("Nepali","nep","ne"),
			new Language("Newari","new","<na>"),
			new Language("Nias","nia","<na>"),
			new Language("Niger-Kordofanian languages","nic","<na>"),
			new Language("Nilo-Saharan languages","ssa","<na>"),
			new Language("Niuean","niu","<na>"),
			new Language("No linguistic content","zxx","<na>"),
			new Language("Nogai","nog","<na>"),
			new Language("Norse, Old","non","<na>"),
			new Language("North American Indian languages","nai","<na>"),
			new Language("North Ndebele","nde","nd"),
			new Language("Northern Frisian","frr","<na>"),
			new Language("Northern Sami","sme","se"),
			new Language("Northern Sotho","nso","<na>"),
			new Language("Norwegian","nor","no"),
			new Language("Norwegian Bokmål","nob","nb"),
			new Language("Norwegian Nynorsk","nno","nn"),
			new Language("Not applicable","zxx","<na>"),
			new Language("Nubian languages","nub","<na>"),
			new Language("Nuosu","iii","ii"),
			new Language("Nyamwezi","nym","<na>"),
			new Language("Nyanja","nya","ny"),
			new Language("Nyankole","nyn","<na>"),
			new Language("Nynorsk, Norwegian","nno","nn"),
			new Language("Nyoro","nyo","<na>"),
			new Language("Nzima","nzi","<na>"),
			new Language("Occidental","ile","ie"),
			new Language("Occitan (post 1500)","oci","oc"),
			new Language("Occitan, Old (to 1500)","pro","<na>"),
			new Language("Official Aramaic (700-300 BCE)","arc","<na>"),
			new Language("Oirat","xal","<na>"),
			new Language("Ojibwa","oji","oj"),
			new Language("Old Bulgarian","chu","cu"),
			new Language("Old Church Slavonic","chu","cu"),
			new Language("Old Newari","nwc","<na>"),
			new Language("Old Slavonic","chu","cu"),
			new Language("Oriya","ori","or"),
			new Language("Oromo","orm","om"),
			new Language("Osage","osa","<na>"),
			new Language("Ossetian","oss","os"),
			new Language("Ossetic","oss","os"),
			new Language("Otomian languages","oto","<na>"),
			new Language("Pahlavi","pal","<na>"),
			new Language("Palauan","pau","<na>"),
			new Language("Pali","pli","pi"),
			new Language("Pampanga","pam","<na>"),
			new Language("Pangasinan","pag","<na>"),
			new Language("Panjabi","pan","pa"),
			new Language("Papiamento","pap","<na>"),
			new Language("Papuan languages","paa","<na>"),
			new Language("Pashto","pus","ps"),
			new Language("Pedi","nso","<na>"),
			new Language("Persian","per","fa"),
			new Language("Persian","fas","fa"),
			new Language("Persian, Old (ca.600-400 B.C.)","peo","<na>"),
			new Language("Philippine languages","phi","<na>"),
			new Language("Phoenician","phn","<na>"),
			new Language("Pilipino","fil","<na>"),
			new Language("Pohnpeian","pon","<na>"),
			new Language("Polish","pol","pl"),
			new Language("Portuguese","por","pt"),
			new Language("Prakrit languages","pra","<na>"),
			new Language("Provençal, Old (to 1500)","pro","<na>"),
			new Language("Punjabi","pan","pa"),
			new Language("Pushto","pus","ps"),
			new Language("Quechua","que","qu"),
			new Language("Rajasthani","raj","<na>"),
			new Language("Rapanui","rap","<na>"),
			new Language("Rarotongan","rar","<na>"),
			new Language("Reserved for local use","qaa-qtz","<na>"),
			new Language("Romance languages","roa","<na>"),
			new Language("Romanian","rum","ro"),
			new Language("Romanian","ron","ro"),
			new Language("Romansh","roh","rm"),
			new Language("Romany","rom","<na>"),
			new Language("Rundi","run","rn"),
			new Language("Russian","rus","ru"),
			new Language("Sakan","kho","<na>"),
			new Language("Salishan languages","sal","<na>"),
			new Language("Samaritan Aramaic","sam","<na>"),
			new Language("Sami languages","smi","<na>"),
			new Language("Samoan","smo","sm"),
			new Language("Sandawe","sad","<na>"),
			new Language("Sango","sag","sg"),
			new Language("Sanskrit","san","sa"),
			new Language("Santali","sat","<na>"),
			new Language("Sardinian","srd","sc"),
			new Language("Sasak","sas","<na>"),
			new Language("Saxon, Low","nds","<na>"),
			new Language("Scots","sco","<na>"),
			new Language("Scottish Gaelic","gla","gd"),
			new Language("Selkup","sel","<na>"),
			new Language("Semitic languages","sem","<na>"),
			new Language("Sepedi","nso","<na>"),
			new Language("Serbian","srp","sr"),
			new Language("Serer","srr","<na>"),
			new Language("Shan","shn","<na>"),
			new Language("Shona","sna","sn"),
			new Language("Sichuan Yi","iii","ii"),
			new Language("Sicilian","scn","<na>"),
			new Language("Sidamo","sid","<na>"),
			new Language("Sign Languages","sgn","<na>"),
			new Language("Siksika","bla","<na>"),
			new Language("Sindhi","snd","sd"),
			new Language("Sinhala","sin","si"),
			new Language("Sinhalese","sin","si"),
			new Language("Sino-Tibetan languages","sit","<na>"),
			new Language("Siouan languages","sio","<na>"),
			new Language("Skolt Sami","sms","<na>"),
			new Language("Slave (Athapascan)","den","<na>"),
			new Language("Slavic languages","sla","<na>"),
			new Language("Slovak","slo","sk"),
			new Language("Slovak","slk","sk"),
			new Language("Slovenian","slv","sl"),
			new Language("Sogdian","sog","<na>"),
			new Language("Somali","som","so"),
			new Language("Songhai languages","son","<na>"),
			new Language("Soninke","snk","<na>"),
			new Language("Sorbian languages","wen","<na>"),
			new Language("Sotho, Northern","nso","<na>"),
			new Language("Sotho, Southern","sot","st"),
			new Language("South American Indian languages","sai","<na>"),
			new Language("South Ndebele","nbl","nr"),
			new Language("Southern Altai","alt","<na>"),
			new Language("Southern Sami","sma","<na>"),
			new Language("Spanish","spa","es"),
			new Language("Sranan Tongo","srn","<na>"),
			new Language("Standard Moroccan Tamazight","zgh","<na>"),
			new Language("Sukuma","suk","<na>"),
			new Language("Sumerian","sux","<na>"),
			new Language("Sundanese","sun","su"),
			new Language("Susu","sus","<na>"),
			new Language("Swahili","swa","sw"),
			new Language("Swati","ssw","ss"),
			new Language("Swedish","swe","sv"),
			new Language("Swiss German","gsw","<na>"),
			new Language("Syriac","syr","<na>"),
			new Language("Tagalog","tgl","tl"),
			new Language("Tahitian","tah","ty"),
			new Language("Tai languages","tai","<na>"),
			new Language("Tajik","tgk","tg"),
			new Language("Tamashek","tmh","<na>"),
			new Language("Tamil","tam","ta"),
			new Language("Tatar","tat","tt"),
			new Language("Telugu","tel","te"),
			new Language("Tereno","ter","<na>"),
			new Language("Tetum","tet","<na>"),
			new Language("Thai","tha","th"),
			new Language("Tibetan","tib","bo"),
			new Language("Tibetan","bod","bo"),
			new Language("Tigre","tig","<na>"),
			new Language("Tigrinya","tir","ti"),
			new Language("Timne","tem","<na>"),
			new Language("Tiv","tiv","<na>"),
			new Language("tlhIngan-Hol","tlh","<na>"),
			new Language("Tlingit","tli","<na>"),
			new Language("Tok Pisin","tpi","<na>"),
			new Language("Tokelau","tkl","<na>"),
			new Language("Tonga (Nyasa)","tog","<na>"),
			new Language("Tonga (Tonga Islands)","ton","to"),
			new Language("Tsimshian","tsi","<na>"),
			new Language("Tsonga","tso","ts"),
			new Language("Tswana","tsn","tn"),
			new Language("Tumbuka","tum","<na>"),
			new Language("Tupi languages","tup","<na>"),
			new Language("Turkish","tur","tr"),
			new Language("Turkish, Ottoman (1500-1928)","ota","<na>"),
			new Language("Turkmen","tuk","tk"),
			new Language("Tuvalu","tvl","<na>"),
			new Language("Tuvinian","tyv","<na>"),
			new Language("Twi","twi","tw"),
			new Language("Udmurt","udm","<na>"),
			new Language("Ugaritic","uga","<na>"),
			new Language("Uighur","uig","ug"),
			new Language("Ukrainian","ukr","uk"),
			new Language("Umbundu","umb","<na>"),
			new Language("Uncoded languages","mis","<na>"),
			new Language("Undetermined","und","<na>"),
			new Language("Upper Sorbian","hsb","<na>"),
			new Language("Urdu","urd","ur"),
			new Language("Uyghur","uig","ug"),
			new Language("Uzbek","uzb","uz"),
			new Language("Vai","vai","<na>"),
			new Language("Valencian","cat","ca"),
			new Language("Venda","ven","ve"),
			new Language("Vietnamese","vie","vi"),
			new Language("Volapük","vol","vo"),
			new Language("Votic","vot","<na>"),
			new Language("Wakashan languages","wak","<na>"),
			new Language("Walloon","wln","wa"),
			new Language("Waray","war","<na>"),
			new Language("Washo","was","<na>"),
			new Language("Welsh","wel","cy"),
			new Language("Welsh","cym","cy"),
			new Language("Western Frisian","fry","fy"),
			new Language("Western Pahari languages","him","<na>"),
			new Language("Wolaitta","wal","<na>"),
			new Language("Wolaytta","wal","<na>"),
			new Language("Wolof","wol","wo"),
			new Language("Xhosa","xho","xh"),
			new Language("Yakut","sah","<na>"),
			new Language("Yao","yao","<na>"),
			new Language("Yapese","yap","<na>"),
			new Language("Yiddish","yid","yi"),
			new Language("Yoruba","yor","yo"),
			new Language("Yupik languages","ypk","<na>"),
			new Language("Zande languages","znd","<na>"),
			new Language("Zapotec","zap","<na>"),
			new Language("Zaza","zza","<na>"),
			new Language("Zazaki","zza","<na>"),
			new Language("Zenaga","zen","<na>"),
			new Language("Zhuang","zha","za"),
			new Language("Zulu","zul","zu"),
			new Language("Zuni","zun","<na>")
		);
	}
}