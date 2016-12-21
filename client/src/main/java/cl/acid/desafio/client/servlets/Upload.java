package cl.acid.desafio.client.servlets;

import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

import javax.imageio.ImageIO;
import javax.servlet.ServletException;
import javax.servlet.annotation.MultipartConfig;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.Part;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.sun.jersey.api.client.Client;
import com.sun.jersey.api.client.ClientResponse;
import com.sun.jersey.api.client.WebResource;

import sun.misc.BASE64Encoder;

/**
 * Servlet implementation class Upload
 */

@MultipartConfig
public class Upload extends HttpServlet
{
	private static final long serialVersionUID = 1L;

	String postUrl = "";

	/**
	 * Default constructor.
	 */
	public Upload()
	{
		
		/**
		 * Cargo la url del rest a traves de la propertie
		 */
		Properties prop = new Properties();
		InputStream input = null;

		try {

			input = this.getClass().getResourceAsStream("/urlRest.properties");
			prop.load(input);
			postUrl = prop.getProperty("urlInsertar");
			
		}catch(Exception ex)
		{
			System.out.println(ex);

		}
	}

	/**
	 * @see HttpServlet#doGet(HttpServletRequest request, HttpServletResponse
	 *      response)
	 */
	protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException
	{
		doWork(request, response);
	}

	/**
	 * @see HttpServlet#doPost(HttpServletRequest request, HttpServletResponse
	 *      response)
	 */
	protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException
	{
		doWork(request, response);
	}

	/**
	 * Proceso las peticiones del servlet
	 * @param request
	 * @param response
	 * @throws IOException
	 */
	private void doWork(HttpServletRequest request, HttpServletResponse response) throws IOException
	{
		try
		{
			String usuario = request.getParameter("usuario");
			String base64 = "";
			Part filePart = request.getPart("file");
			if(filePart.getSize() > 0)
			{
				BufferedImage imagen = ImageIO.read(filePart.getInputStream());
				base64 = encodeToString(imagen, "jpg");
			}
			
			ObjectMapper mapper = new ObjectMapper();

			ObjectNode objectNode = mapper.createObjectNode();
			objectNode.put("username", usuario);
			objectNode.put("image", base64);

			ClientResponse respuesta = postRequest(mapper.writeValueAsString(objectNode));
			response.setStatus(respuesta.getStatus()); 
			if(response.getStatus() == 201)
			{
				response.setHeader("location",respuesta.getHeaders().get("location").toString());
			}
				
			String text = respuesta.getEntity(String.class);
			response.getWriter().append(text);
			
			

		} catch (ServletException e)
		{
			e.printStackTrace();
		}

	}

	/**
	 * Metodo para realizar la llamada al rest de insercion de imagen
	 * @param json
	 * @return
	 */
	public ClientResponse postRequest(String json)
	{
		Client client = Client.create();
		WebResource webResource = client.resource(postUrl);

		ClientResponse response = webResource.type("application/json").post(ClientResponse.class, json);
		return response;
	}

	/**
	 * Metodo para transformar una imagen a base64
	 * @param image
	 * @param type
	 * @return
	 */
	public String encodeToString(BufferedImage image, String type)
	{
		String imageString = null;
		ByteArrayOutputStream bos = new ByteArrayOutputStream();

		try
		{
			ImageIO.write(image, type, bos);
			byte[] imageBytes = bos.toByteArray();

			BASE64Encoder encoder = new BASE64Encoder();
			imageString = encoder.encode(imageBytes);

			bos.close();
		} catch (IOException e)
		{
			e.printStackTrace();
		}
		return imageString;
	}
	

}
