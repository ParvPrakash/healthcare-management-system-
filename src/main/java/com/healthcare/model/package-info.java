/**
 * Domain entity models and core business abstractions.
 * <p>
 * Implements a polymorphic domain model rooted at {@link com.healthcare.model.User},
 * with specialized sub-entities {@link com.healthcare.model.Doctor},
 * {@link com.healthcare.model.Patient}, and {@link com.healthcare.model.Admin}.
 * Defines contracts including {@link com.healthcare.model.Authenticatable},
 * {@link com.healthcare.model.Bookable}, {@link com.healthcare.model.Notifiable},
 * and {@link com.healthcare.model.Reportable}, along with strong domain enums
 * {@link com.healthcare.model.Role} and {@link com.healthcare.model.AppointmentStatus}.
 * </p>
 */
package com.healthcare.model;
