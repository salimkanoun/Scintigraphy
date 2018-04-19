import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

public class teest {

	public static void main(String[] args) throws ParseException {
		String target = "20160303";
		Date result = new SimpleDateFormat("yyyymmdd").parse(target);
		String r = new SimpleDateFormat("MMM dd yyyy", Locale.ENGLISH).format(result);
		System.out.println(r);
	}

}
