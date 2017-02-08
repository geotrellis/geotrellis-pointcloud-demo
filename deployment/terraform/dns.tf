resource "aws_route53_zone" "external" {
  name = "${var.r53_public_hosted_zone_name}"

  tags {
    Project     = "${var.project}"
    Environment = "${var.environment}"
  }
}

resource "aws_route53_record" "site" {
  zone_id = "${aws_route53_zone.external.id}"
  name    = "${var.r53_public_hosted_zone_name}"
  type    = "A"

  alias {
    name                   = "${aws_cloudfront_distribution.cdn.domain_name}"
    zone_id                = "${aws_cloudfront_distribution.cdn.hosted_zone_id}"
    evaluate_target_health = false
  }
}
