package net.globulus.easycopysample;

import net.globulus.easycopy.annotation.EasyCopy;
import net.globulus.easycopy.annotation.Skip;

import java.util.Date;

/**
 * Created by gordanglavas on 30/09/16.
 */
@EasyCopy(deep = true)
public class Goal {

	public int goalType;

	public Date endDate;

	@Skip
	public int parameter1;

	@Skip
	public int parameter2;

	public Float carbs;

	public Float calories;

	public Float fat;

	public Float protein;

	public String name;

	public Trolo trolol;
}
