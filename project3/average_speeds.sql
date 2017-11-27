WITH average_edge_speeds AS
(SELECT cleaned_edge, edge_type, AVG(speed) AS average_speed
FROM bdm_proj_3
GROUP BY cleaned_edge, edge_type)

SELECT cleaned_edge, edge_type, average_speed
FROM average_edge_speeds
WHERE average_speed >= 11 AND average_speed <= 12
ORDER BY average_speed DESC
LIMIT 10