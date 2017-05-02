# funnypro

��������� ��� ���� - ������������� ����� Prolog. ������ ���������� � ���������� ������������� �������� ������.
������������� ����� ����������� ��� ��������� ����������, � ����� ������������ � Java �� ������������ SPI. 
������� � �������� ������� ��� ������ chav1961.funnypro.Application:

public class Application {
	public static void main(String[] args) throws ScriptException {
		try(final Reader	in = new InputStreamReader(System.in);
			final Writer	out = new OutputStreamWriter(System.out); 
			final Writer	err = new OutputStreamWriter(System.err)) {
			
 			final ScriptEngineManager 	factory = new ScriptEngineManager();
			final ScriptEngine 			engine = factory.getEngineByName("FunnyProlog");

 			((FunnyProEngine)engine).console(in,out,err);
		} catch (Exception exc) {
			exc.printStackTrace();
		}
	}
}

����� ��������� �������� �� ���� � �������. ������� ��������� ���, ��������� ���������� � ������� ���-����� � ��������������
��� � �� ����� - ��� ����� ���� ���������� ���� Java (�� ������� ������������� � �������).
