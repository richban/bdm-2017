WITH number_of_pedestrians AS
(SELECT cleaned_edge, simulation_step_number, COUNT(DISTINCT traffic_id) AS pedestrian_count
FROM bdm_proj_3
WHERE traffic_type = 'person'
GROUP BY cleaned_edge, simulation_step_number
ORDER BY cleaned_edge, simulation_step_number, pedestrian_count),

number_of_vehicles AS
(SELECT cleaned_edge, simulation_step_number, COUNT(DISTINCT traffic_id) AS vehicle_count
FROM bdm_proj_3
WHERE traffic_type = 'vehicle'
GROUP BY cleaned_edge, simulation_step_number
ORDER BY cleaned_edge, simulation_step_number, vehicle_count)

SELECT number_of_pedestrians.cleaned_edge, AVG(number_of_pedestrians.pedestrian_count) AS average_pedestrians,
AVG(number_of_vehicles.vehicle_count) AS average_vehicles,
AVG(number_of_pedestrians.pedestrian_count) / (1.0 * AVG(number_of_vehicles.vehicle_count)) AS ratio
FROM number_of_pedestrians
JOIN number_of_vehicles ON number_of_pedestrians.cleaned_edge = number_of_vehicles.cleaned_edge
GROUP BY number_of_pedestrians.cleaned_edge
ORDER BY ratio DESC

/*SELECT bdm_proj_3.cleaned_edge, (number_of_pedestrians.pedestrian_count) / (1.0 * COUNT(DISTINCT bdm_proj_3.traffic_id)) AS pedestrian_vehicle_ratio
FROM bdm_proj_3
JOIN number_of_pedestrians ON bdm_proj_3.cleaned_edge = number_of_pedestrians.cleaned_edge
WHERE bdm_proj_3.traffic_type = 'vehicle'
GROUP BY bdm_proj_3.cleaned_edge, number_of_pedestrians.pedestrian_count
ORDER BY pedestrian_vehicle_ratio DESC*/